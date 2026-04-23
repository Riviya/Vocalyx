import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import NavigationBar from '@/components/NavigationBar';
import { 
  Upload, 
  Building2, 
  Users, 
  Brain, 
  CheckCircle, 
  ArrowRight, 
  ArrowLeft,
  FileAudio,
  Calendar,
  Clock,
  User,
  Phone,
  Mail,
  Briefcase
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import FileUploadComponent from '@/components/FileUploadComponent';
import CompanyManagement from '@/components/CompanyManagement';
import ContactManagement from '@/components/ContactManagement';
import SentimentResults from '@/components/SentimentResults';

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
  transcript: string;
  summary: string;
  sentimentScore: number;
  sentimentPercentage: number;
  sentimentType: 'VERY_POSITIVE' | 'POSITIVE' | 'NEUTRAL' | 'NEGATIVE' | 'VERY_NEGATIVE';
  sentimentAnalysis: string;
  fileSize: number;
  fileType: string;
  companyName: string;
  contactId: number;
  contactName: string;
  userId: number;
  userName: string;
  orderId?: number;
  createdAt: string;
  updatedAt: string;
}

const SentimentAnalysis = () => {
  const { toast } = useToast();
  const navigate = useNavigate();
  const [currentStage, setCurrentStage] = useState(1);
  const [uploadedFile, setUploadedFile] = useState<UploadedFile | null>(null);
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null);
  const [companyName, setCompanyName] = useState<string>('');
  const [callData, setCallData] = useState<CallData | null>(null);
  const [sentimentResult, setSentimentResult] = useState<SentimentResult | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const stages = [
    { id: 1, title: 'Upload Call', description: 'Upload your call recording', icon: Upload },
    { id: 2, title: 'Contact Info', description: 'Get contact info and optional company name', icon: Users },
    { id: 3, title: 'Save & Analyze', description: 'Save call info and analyze sentiment', icon: Brain }
  ];

  const handleFileUpload = (file: UploadedFile) => {
    setUploadedFile(file);
    toast({
      title: "File Uploaded",
      description: `Successfully uploaded ${file.file.name}`,
    });
  };

  const handleContactSelect = (contact: Contact) => {
    setSelectedContact(contact);
    toast({
      title: "Contact Selected",
      description: `Selected ${contact.firstName} ${contact.lastName}`,
    });
  };

  const handleCallDataSubmit = (data: CallData) => {
    setCallData(data);
    toast({
      title: "Call Data Ready",
      description: "Ready to analyze sentiment",
    });
  };

  const handleSentimentAnalysis = async (result: SentimentResult) => {
    setSentimentResult(result);
    toast({
      title: "Analysis Complete",
      description: `Sentiment: ${result.sentimentType}`,
    });
  };

  const handleDealClosed = (dealData: any) => {
    if (dealData.isDealClosed) {
      toast({
        title: "Deal Closed Successfully!",
        description: `Revenue: $${dealData.saleAmount}`,
      });
    } else {
      toast({
        title: "Deal Not Closed",
        description: "This call did not result in a closed deal.",
      });
    }
  };

  const nextStage = () => {
    if (currentStage < 3) {
      setCurrentStage(currentStage + 1);
    } else if (currentStage === 3) {
      navigate('/dashboard');
    }
  };

  const prevStage = () => {
    if (currentStage > 1) {
      setCurrentStage(currentStage - 1);
    }
  };

  const resetWorkflow = () => {
    setCurrentStage(1);
    setUploadedFile(null);
    setSelectedContact(null);
    setCompanyName('');
    setCallData(null);
    setSentimentResult(null);
  };

  const getStageStatus = (stageId: number) => {
    if (stageId < currentStage) return 'completed';
    if (stageId === currentStage) return 'current';
    return 'pending';
  };

  const getProgressPercentage = () => {
    return (currentStage / 3) * 100;
  };

  return (
    <div className="min-h-screen bg-background">
      <NavigationBar />
      
      {/* Page Header */}
      <div className="bg-background border-b border-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-foreground">Sentiment Analysis</h1>
              <p className="text-muted-foreground mt-2">Analyze call recordings and track customer sentiment</p>
            </div>
            <Button 
              variant="outline" 
              onClick={resetWorkflow}
            >
              Start Over
            </Button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Progress Indicator */}
        <Card className="mb-8">
          <CardContent className="pt-6">
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium">Progress</span>
                <span className="text-sm text-muted-foreground">{currentStage} of 3 stages</span>
              </div>
              <Progress value={getProgressPercentage()} className="h-2" />
              
              {/* Stage Indicators */}
              <div className="flex justify-between">
                {stages.map((stage) => {
                  const status = getStageStatus(stage.id);
                  const Icon = stage.icon;
                  
                  return (
                    <div key={stage.id} className="flex flex-col items-center space-y-2">
                      <div className={`
                        w-10 h-10 rounded-full flex items-center justify-center border-2
                        ${status === 'completed' ? 'bg-green-500 border-green-500 text-white' : 
                          status === 'current' ? 'bg-primary border-primary text-white' : 
                          'bg-muted border-muted-foreground text-muted-foreground'}
                      `}>
                        {status === 'completed' ? (
                          <CheckCircle className="h-5 w-5" />
                        ) : (
                          <Icon className="h-5 w-5" />
                        )}
                      </div>
                      <div className="text-center">
                        <p className={`text-xs font-medium ${
                          status === 'current' ? 'text-primary' : 
                          status === 'completed' ? 'text-green-600' : 'text-muted-foreground'
                        }`}>
                          {stage.title}
                        </p>
                        <p className="text-xs text-muted-foreground">{stage.description}</p>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Stage Content */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  {React.createElement(stages[currentStage - 1].icon, { className: "h-5 w-5" })}
                  <span>Stage {currentStage}: {stages[currentStage - 1].title}</span>
                </CardTitle>
                <CardDescription>{stages[currentStage - 1].description}</CardDescription>
              </CardHeader>
              <CardContent>
                {currentStage === 1 && (
                  <FileUploadComponent 
                    onFileUpload={handleFileUpload}
                    uploadedFile={uploadedFile}
                  />
                )}
                
                {currentStage === 2 && (
                  <ContactManagement 
                    onContactSelect={handleContactSelect}
                    selectedContact={selectedContact}
                    onCompanyNameChange={setCompanyName}
                    companyName={companyName}
                  />
                )}
                
                {currentStage === 3 && (
                  <SentimentResults 
                    uploadedFile={uploadedFile}
                    selectedContact={selectedContact}
                    companyName={companyName}
                    onCallDataSubmit={handleCallDataSubmit}
                    onSentimentAnalysis={handleSentimentAnalysis}
                    sentimentResult={sentimentResult}
                    isLoading={isLoading}
                    setIsLoading={setIsLoading}
                    onDealClosed={handleDealClosed}
                  />
                )}
              </CardContent>
            </Card>
          </div>

          {/* Summary Sidebar */}
          <div className="space-y-6">
            {/* Upload Summary */}
            {uploadedFile && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center space-x-2">
                    <FileAudio className="h-5 w-5" />
                    <span>Uploaded File</span>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  <p className="text-sm font-medium">{uploadedFile.file.name}</p>
                  <p className="text-xs text-muted-foreground">
                    {(uploadedFile.fileSize / (1024 * 1024)).toFixed(2)} MB
                  </p>
                  <Badge variant="secondary">{uploadedFile.contentType}</Badge>
                </CardContent>
              </Card>
            )}


            {/* Contact Summary */}
            {selectedContact && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center space-x-2">
                    <Users className="h-5 w-5" />
                    <span>Selected Contact</span>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  <p className="text-sm font-medium">
                    {selectedContact.salutation} {selectedContact.firstName} {selectedContact.lastName}
                  </p>
                  <p className="text-xs text-muted-foreground">{selectedContact.jobTitle}</p>
                  <Badge variant="outline">{selectedContact.department}</Badge>
                  {companyName && (
                    <div className="mt-2">
                      <p className="text-xs text-muted-foreground">Company:</p>
                      <p className="text-sm font-medium">{companyName}</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            {/* Sentiment Result Summary */}
            {sentimentResult && (
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center space-x-2">
                    <Brain className="h-5 w-5" />
                    <span>Analysis Result</span>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium">Sentiment Score:</span>
                    <span className="text-sm font-bold">
                      {sentimentResult.sentimentPercentage || sentimentResult.sentimentScore?.toFixed(0)}%
                    </span>
                  </div>
                  <Badge 
                    variant={
                      sentimentResult.sentimentType === 'VERY_POSITIVE' || sentimentResult.sentimentType === 'POSITIVE' 
                        ? 'default' 
                        : sentimentResult.sentimentType === 'NEUTRAL' 
                        ? 'secondary' 
                        : 'destructive'
                    }
                  >
                    {sentimentResult.sentimentType.replace('_', ' ')}
                  </Badge>
                </CardContent>
              </Card>
            )}
          </div>
        </div>

        {/* Navigation Buttons */}
        <div className="flex justify-between mt-8">
          <Button 
            variant="outline" 
            onClick={prevStage}
            disabled={currentStage === 1}
            className="flex items-center space-x-2"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Previous</span>
          </Button>
          
          <Button 
            onClick={nextStage}
            disabled={
              (currentStage === 1 && !uploadedFile) ||
              (currentStage === 2 && !selectedContact)
            }
            className="flex items-center space-x-2"
          >
            <span>{currentStage === 3 ? 'Complete' : 'Next'}</span>
            {currentStage < 3 && <ArrowRight className="h-4 w-4" />}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default SentimentAnalysis;
