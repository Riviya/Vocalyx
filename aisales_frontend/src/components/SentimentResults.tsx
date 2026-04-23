import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { 
  Brain, 
  TrendingUp, 
  TrendingDown, 
  Minus, 
  CheckCircle, 
  AlertCircle,
  Loader2,
  BarChart3,
  FileText,
  MessageSquare,
  DollarSign,
  Calendar,
  User,
  Building
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

type SentimentType = 'VERY_POSITIVE' | 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE' | 'VERY_NEGATIVE';

interface UploadedFile {
  file: File;
  filePath: string;
  fileSize: number;
  contentType: string;
}

interface Company {
  id: number;
  companyName: string;
  type: 'CLIENT' | 'SUPPLIER' | 'PROSPECT';
  industry: string;
  status: string;
  priority: string;
  createdAt: string;
}

interface Contact {
  id: number;
  salutation: string;
  firstName: string;
  lastName: string;
  jobTitle: string;
  email: string;
  phoneNumber: string;
  department: string;
  status: string;
  companyId: number;
  companyName: string;
}

interface CallData {
  callTitle: string;
  callDateTime: Date;
  callDirection: 'OUTGOING' | 'INCOMING';
  summary: string;
  contactId: number;
  recordingFilePath: string;
  fileSize: number;
  fileType: string;
}

interface SentimentResult {
  id: number;
  callTitle: string;
  callDateTime: string;
  recordingFilePath: string;
  callDirection: 'OUTGOING' | 'INCOMING';
  summary: string;
  transcript: string;
  sentimentScore: number;
  sentimentPercentage: number;
  sentimentType: SentimentType;
  sentimentAnalysis: string;
  fileSize: number;
  fileType: string;
  companyName: string;
  contactId: number;
  contactName: string;
  userId: number;
  userName: string;
  orderId?: number;
  orderNumber?: string;
  createdAt: string;
  updatedAt: string;
}

interface DealClosureData {
  isDealClosed: boolean;
  saleAmount: string;
  closedDate: string;
  notes: string;
}

interface Props {
  uploadedFile: UploadedFile | null;
  selectedContact: Contact | null;
  companyName: string;
  onCallDataSubmit: (data: CallData) => void;
  onSentimentAnalysis: (result: SentimentResult) => void;
  sentimentResult: SentimentResult | null;
  isLoading: boolean;
  setIsLoading: (loading: boolean) => void;
  onDealClosed?: (dealData: DealClosureData) => void;
}

const SentimentResults: React.FC<Props> = ({
  uploadedFile,
  selectedContact,
  companyName,
  onCallDataSubmit,
  onSentimentAnalysis,
  sentimentResult,
  isLoading,
  setIsLoading,
  onDealClosed
}) => {
  const { toast } = useToast();
  const [callDirection, setCallDirection] = useState<'OUTGOING' | 'INCOMING'>('OUTGOING');
  const [summary, setSummary] = useState<string>('');
  const [callTitle, setCallTitle] = useState<string>('');
  
  // Deal closure state
  const [showDealClosure, setShowDealClosure] = useState<boolean>(false);
  const [dealClosureData, setDealClosureData] = useState<DealClosureData>({
    isDealClosed: false,
    saleAmount: '',
    closedDate: new Date().toISOString().split('T')[0], // Today's date in YYYY-MM-DD format
    notes: ''
  });
  const [isSubmittingDeal, setIsSubmittingDeal] = useState<boolean>(false);
  const [dealClosureCompleted, setDealClosureCompleted] = useState<boolean>(false);

  const handleSaveCallData = async () => {
    if (!uploadedFile || !selectedContact) return;

    setIsLoading(true);

    try {
      // Step 1: First save the call data to our backend
      const callPayload = {
        callTitle: callTitle || 'Untitled Call',
        callDateTime: new Date().toISOString(),
        callDirection,
        summary,
        contactId: selectedContact.id,
        recordingFilePath: uploadedFile.filePath,
        fileSize: uploadedFile.fileSize,
        fileType: uploadedFile.contentType,
        companyName: companyName || '',
        userId: 1, // This should come from auth context
        orderId: null
      };

      const token = localStorage.getItem('finsight_token');
      const callResponse = await fetch('http://localhost:8080/api/calls', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(callPayload),
      });

      if (!callResponse.ok) {
        throw new Error('Failed to save call data to backend');
      }

      const callResult = await callResponse.json();
      const callId = callResult.id;

      toast({
        title: "Call Saved",
        description: "Call data saved successfully. Analysis in progress...",
      });

      // Simulate the call data submission
      const payload: CallData = {
        callTitle: callTitle || 'Untitled Call',
        callDateTime: new Date(),
        callDirection,
        summary,
        contactId: selectedContact.id,
        recordingFilePath: uploadedFile.filePath,
        fileSize: uploadedFile.fileSize,
        fileType: uploadedFile.contentType,
      };
      
      onCallDataSubmit(payload);

      // Start polling for results (the backend will handle N8N workflow)
      pollForSentimentResults(callId);
    } catch (error) {
      console.error('Error in save call data:', error);
      toast({
        title: "Error",
        description: "Failed to save call data",
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Helper function to map sentiment labels to our enum
  const mapSentimentLabel = (label: string): 'VERY_POSITIVE' | 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE' | 'VERY_NEGATIVE' => {
    const lowerLabel = label?.toLowerCase() || '';
    if (lowerLabel.includes('extremely positive') || lowerLabel.includes('very positive')) return 'VERY_POSITIVE';
    if (lowerLabel.includes('positive')) return 'POSITIVE';
    if (lowerLabel.includes('negative')) return 'NEGATIVE';
    if (lowerLabel.includes('very negative') || lowerLabel.includes('extremely negative')) return 'VERY_NEGATIVE';
    return 'NEUTRAL';
  };

  const pollForSentimentResults = async (callId: number) => {
    const maxAttempts = 30; // Poll for up to 5 minutes (10 seconds * 30)
    let attempts = 0;

    const poll = async () => {
      try {
        const token = localStorage.getItem('finsight_token');
        const response = await fetch(`http://localhost:8080/api/calls/${callId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok) {
          const result = await response.json();
          console.log('Polling result:', result);
          
          // Check if sentiment analysis is complete
          if (result.sentimentPercentage !== null && result.sentimentPercentage !== undefined && 
              result.sentimentLabel && result.transcript) {
            console.log('Sentiment analysis complete, calling onSentimentAnalysis');
            
            // Map the backend response to frontend format
            const sentimentResult: SentimentResult = {
              id: result.id,
              callTitle: result.callTitle,
              callDateTime: result.callDateTime,
              recordingFilePath: result.recordingFilePath,
              callDirection: result.callDirection,
              summary: result.summary,
              transcript: result.transcript,
              sentimentScore: result.sentimentPercentage,
              sentimentPercentage: result.sentimentPercentage,
              sentimentType: mapSentimentLabel(result.sentimentLabel),
              sentimentAnalysis: result.sentimentLabel,
              fileSize: result.fileSize,
              fileType: result.fileType,
              companyName: result.companyName,
              contactId: result.contactId,
              contactName: result.contactName,
              userId: result.userId,
              userName: result.userName,
              orderId: result.orderId,
              orderNumber: result.orderNumber,
              createdAt: result.createdAt,
              updatedAt: result.updatedAt
            };
            
            onSentimentAnalysis(sentimentResult);
            toast({
              title: "Analysis Complete",
              description: `Sentiment: ${result.sentimentLabel}`,
            });
            return;
          } else {
            console.log('Sentiment analysis not complete yet, continuing to poll...');
          }
        } else {
          console.error('Polling failed with status:', response.status);
        }

        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(poll, 10000); // Poll every 10 seconds
        } else {
          toast({
            title: "Analysis Timeout",
            description: "Sentiment analysis is taking longer than expected",
            variant: "destructive",
          });
        }
      } catch (error) {
        console.error('Error polling for results:', error);
        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(poll, 10000);
        }
      }
    };

    // Start polling after a short delay
    setTimeout(poll, 5000);
  };

  const getSentimentColor = (sentimentType: SentimentType) => {
    switch (sentimentType) {
      case 'VERY_POSITIVE':
        return 'bg-green-100 text-green-800 border-green-200';
      case 'POSITIVE':
        return 'bg-green-50 text-green-700 border-green-100';
      case 'NEUTRAL':
        return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'NEGATIVE':
        return 'bg-red-50 text-red-700 border-red-100';
      case 'VERY_NEGATIVE':
        return 'bg-red-100 text-red-800 border-red-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const getSentimentIcon = (sentimentType: SentimentType) => {
    switch (sentimentType) {
      case 'VERY_POSITIVE':
      case 'POSITIVE':
        return <TrendingUp className="h-4 w-4" />;
      case 'NEUTRAL':
        return <Minus className="h-4 w-4" />;
      case 'NEGATIVE':
      case 'VERY_NEGATIVE':
        return <TrendingDown className="h-4 w-4" />;
      default:
        return <Minus className="h-4 w-4" />;
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    if (score >= 40) return 'text-orange-600';
    return 'text-red-600';
  };

  const handleDealClosureSubmit = async () => {
    if (!dealClosureData.saleAmount || !dealClosureData.closedDate) {
      toast({
        title: "Missing Information",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }

    setIsSubmittingDeal(true);

    try {
      // Create sales log request
      const salesLogRequest = {
        customerName: selectedContact ? `${selectedContact.firstName} ${selectedContact.lastName}` : 'Unknown Customer',
        companyDetails: companyName || selectedContact?.companyName || '',
        saleAmount: parseFloat(dealClosureData.saleAmount),
        closedDate: dealClosureData.closedDate,
        callId: sentimentResult?.id || null,
        orderId: sentimentResult?.orderId || null,
        contactId: selectedContact?.id || null,
        userId: 1, // This should come from auth context
        notes: dealClosureData.notes
      };

      const token = localStorage.getItem('finsight_token');
      const response = await fetch('http://localhost:8080/api/sales-log', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(salesLogRequest),
      });

      if (!response.ok) {
        throw new Error('Failed to create sales log');
      }

      const salesLogResult = await response.json();
      
      toast({
        title: "Deal Closed Successfully!",
        description: `Sales log created for $${dealClosureData.saleAmount}`,
      });

      // Call the parent callback if provided
      if (onDealClosed) {
        onDealClosed(dealClosureData);
      }

      // Mark deal closure as completed
      setDealClosureCompleted(true);

      // Reset form
      setDealClosureData({
        isDealClosed: true,
        saleAmount: '',
        closedDate: new Date().toISOString().split('T')[0],
        notes: ''
      });
      setShowDealClosure(false);

    } catch (error) {
      console.error('Error creating sales log:', error);
      toast({
        title: "Error",
        description: "Failed to close deal. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsSubmittingDeal(false);
    }
  };

  const handleDealClosureChange = (field: keyof DealClosureData, value: string | boolean) => {
    setDealClosureData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleDealNotClosed = () => {
    // Mark deal closure as completed
    setDealClosureCompleted(true);
    
    // Call the parent callback if provided
    if (onDealClosed) {
      onDealClosed({ ...dealClosureData, isDealClosed: false });
    }
  };

  return (
    <div className="space-y-6">
      {/* Call Details Form */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg flex items-center space-x-2">
            <Brain className="h-5 w-5" />
            <span>Call Details for Analysis</span>
          </CardTitle>
          <CardDescription>Configure call details and initiate sentiment analysis</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="callTitle">Call Title *</Label>
              <input
                id="callTitle"
                type="text"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-primary"
                placeholder="Enter call title"
                value={callTitle}
                onChange={(e) => setCallTitle(e.target.value)}
                required
              />
            </div>

            <div className="space-y-2">
              <Label>Call Direction</Label>
              <div className="flex items-center gap-6 text-sm">
                <label className="inline-flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="direction"
                    value="OUTGOING"
                    checked={callDirection === 'OUTGOING'}
                    onChange={() => setCallDirection('OUTGOING')}
                    className="text-primary"
                  />
                  Outgoing
                </label>
                <label className="inline-flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="direction"
                    value="INCOMING"
                    checked={callDirection === 'INCOMING'}
                    onChange={() => setCallDirection('INCOMING')}
                    className="text-primary"
                  />
                  Incoming
                </label>
              </div>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="summary">Summary / Key points (optional)</Label>
            <Textarea
              id="summary"
              placeholder="Add a short note before analysis..."
              rows={3}
              value={summary}
              onChange={(e) => setSummary(e.target.value)}
            />
          </div>

          <div className="flex gap-3">
            <Button 
              onClick={handleSaveCallData}
              disabled={isLoading || !callTitle.trim()}
              className="flex items-center space-x-2"
            >
              {isLoading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  <span>Processing...</span>
                </>
              ) : (
                <>
                  <Brain className="h-4 w-4" />
                  <span>Analyze Sentiment</span>
                </>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Sentiment Analysis Results */}
      {sentimentResult && (
        <div className="space-y-6">
          {/* Sentiment Overview */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg flex items-center space-x-2">
                <BarChart3 className="h-5 w-5" />
                <span>Sentiment Analysis Results</span>
              </CardTitle>
              <CardDescription>AI-powered analysis of your call recording</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Sentiment Score and Type */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <Label className="text-sm font-medium">Sentiment Score</Label>
                    <span className={`text-2xl font-bold ${getScoreColor(sentimentResult.sentimentPercentage || sentimentResult.sentimentScore)}`}>
                      {sentimentResult.sentimentPercentage || sentimentResult.sentimentScore?.toFixed(0) || 'N/A'}%
                    </span>
                  </div>
                  <Progress 
                    value={sentimentResult.sentimentPercentage || sentimentResult.sentimentScore || 0} 
                    className="h-3"
                  />
                  <p className="text-xs text-muted-foreground">Out of 100</p>
                </div>

                <div className="space-y-3">
                  <Label className="text-sm font-medium">Sentiment Type</Label>
                  <Badge 
                    className={`${getSentimentColor(sentimentResult.sentimentType)} flex items-center space-x-2 w-fit`}
                  >
                    {getSentimentIcon(sentimentResult.sentimentType)}
                    <span>{sentimentResult.sentimentType?.replace('_', ' ') || 'Unknown'}</span>
                  </Badge>
                </div>
              </div>

              {/* Summary */}
              {sentimentResult.summary && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium flex items-center space-x-2">
                    <FileText className="h-4 w-4" />
                    <span>Call Summary</span>
                  </Label>
                  <div className="p-4 bg-muted/50 rounded-lg">
                    <p className="text-sm">{sentimentResult.summary}</p>
                  </div>
                </div>
              )}

              {/* Detailed Analysis */}
              {sentimentResult.sentimentAnalysis && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium flex items-center space-x-2">
                    <MessageSquare className="h-4 w-4" />
                    <span>Detailed Analysis</span>
                  </Label>
                  <div className="p-4 bg-muted/50 rounded-lg">
                    <p className="text-sm whitespace-pre-wrap">{sentimentResult.sentimentAnalysis}</p>
                  </div>
                </div>
              )}

              {/* Transcript */}
              {sentimentResult.transcript && (
                <div className="space-y-2">
                  <Label className="text-sm font-medium">Call Transcript</Label>
                  <div className="p-4 bg-muted/50 rounded-lg max-h-60 overflow-y-auto">
                    <p className="text-sm whitespace-pre-wrap">{sentimentResult.transcript}</p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Call Information */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Call Information</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                <div>
                  <Label className="text-muted-foreground">Call Title</Label>
                  <p className="font-medium">{sentimentResult.callTitle}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Direction</Label>
                  <p className="font-medium">{sentimentResult.callDirection}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Company</Label>
                  <p className="font-medium">{sentimentResult.companyName}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Contact</Label>
                  <p className="font-medium">{sentimentResult.contactName}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Call Date</Label>
                  <p className="font-medium">{new Date(sentimentResult.callDateTime).toLocaleString()}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">File Size</Label>
                  <p className="font-medium">{(sentimentResult.fileSize / (1024 * 1024)).toFixed(2)} MB</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Order Number</Label>
                  <p className="font-medium">{sentimentResult.orderNumber || 'N/A'}</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}


      <Card>
        <CardHeader>
          <CardTitle className="text-lg flex items-center space-x-2">
            <DollarSign className="h-5 w-5" />
            <span>Deal Closure</span>
          </CardTitle>
          <CardDescription>
            Was this call successful? Close the deal and track your revenue.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {dealClosureCompleted ? (
            <div className="text-center py-8">
              <div className="flex items-center justify-center space-x-2 text-green-600 mb-2">
                <CheckCircle className="h-6 w-6" />
                <span className="text-lg font-semibold">Deal Closure Completed</span>
              </div>
              <p className="text-muted-foreground">
                {dealClosureData.isDealClosed 
                  ? `Deal closed successfully for $${dealClosureData.saleAmount}` 
                  : "Deal marked as not closed"}
              </p>
            </div>
          ) : !showDealClosure ? (
            <div className="flex items-center justify-center space-x-4">
              <Button
                onClick={() => setShowDealClosure(true)}
                className="flex items-center space-x-2 bg-green-600 hover:bg-green-700"
              >
                <CheckCircle className="h-4 w-4" />
                <span>Yes, Close This Deal</span>
              </Button>
              <Button
                variant="outline"
                onClick={handleDealNotClosed}
              >
                No, Deal Not Closed
              </Button>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="saleAmount" className="flex items-center space-x-2">
                    <DollarSign className="h-4 w-4" />
                    <span>Sale Amount *</span>
                  </Label>
                  <Input
                    id="saleAmount"
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    value={dealClosureData.saleAmount}
                    onChange={(e) => handleDealClosureChange('saleAmount', e.target.value)}
                    className="w-full"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="closedDate" className="flex items-center space-x-2">
                    <Calendar className="h-4 w-4" />
                    <span>Closed Date *</span>
                  </Label>
                  <Input
                    id="closedDate"
                    type="date"
                    value={dealClosureData.closedDate}
                    onChange={(e) => handleDealClosureChange('closedDate', e.target.value)}
                    className="w-full"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="customerName" className="flex items-center space-x-2">
                  <User className="h-4 w-4" />
                  <span>Customer Name</span>
                </Label>
                <Input
                  id="customerName"
                  type="text"
                  value={selectedContact ? `${selectedContact.firstName} ${selectedContact.lastName}` : 'Unknown Customer'}
                  disabled
                  className="w-full bg-gray-50"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="companyDetails" className="flex items-center space-x-2">
                  <Building className="h-4 w-4" />
                  <span>Company Details</span>
                </Label>
                <Input
                  id="companyDetails"
                  type="text"
                  value={companyName || selectedContact?.companyName || ''}
                  disabled
                  className="w-full bg-gray-50"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="dealNotes">Notes (Optional)</Label>
                <Textarea
                  id="dealNotes"
                  placeholder="Add any additional notes about this deal..."
                  rows={3}
                  value={dealClosureData.notes}
                  onChange={(e) => handleDealClosureChange('notes', e.target.value)}
                />
              </div>

              <div className="flex items-center justify-between pt-4">
                <Button
                  variant="outline"
                  onClick={() => setShowDealClosure(false)}
                >
                  Cancel
                </Button>
                <Button
                  onClick={handleDealClosureSubmit}
                  disabled={isSubmittingDeal || !dealClosureData.saleAmount || !dealClosureData.closedDate}
                  className="flex items-center space-x-2 bg-green-600 hover:bg-green-700"
                >
                  {isSubmittingDeal ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      <span>Closing Deal...</span>
                    </>
                  ) : (
                    <>
                      <CheckCircle className="h-4 w-4" />
                      <span>Close Deal & Create Sales Log</span>
                    </>
                  )}
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default SentimentResults;

 
