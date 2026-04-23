import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Building2, 
  Search, 
  Plus, 
  CheckCircle, 
  AlertCircle,
  Users,
  TrendingUp,
  Calendar
} from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

interface Company {
  id: number;
  companyName: string;
  type: 'CLIENT' | 'SUPPLIER' | 'PROSPECT';
  industry: string;
  status: string;
  priority: string;
  createdAt: string;
}

interface CompanyManagementProps {
  onCompanySelect: (company: Company) => void;
  selectedCompany: Company | null;
}

const CompanyManagement: React.FC<CompanyManagementProps> = ({
  onCompanySelect,
  selectedCompany
}) => {
  const { toast } = useToast();
  const [companies, setCompanies] = useState<Company[]>([]);
  const [filteredCompanies, setFilteredCompanies] = useState<Company[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<string>('ALL');
  const [isLoading, setIsLoading] = useState(false);
  const [isCreating, setIsCreating] = useState(false);

  // Form state for creating new company
  const [newCompany, setNewCompany] = useState({
    companyName: '',
    type: 'CLIENT' as 'CLIENT' | 'SUPPLIER' | 'PROSPECT',
    industry: 'TECHNOLOGY',
    status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE' | 'ON_HOLD',
    priority: 'MEDIUM' as 'HIGH' | 'MEDIUM' | 'LOW'
  });

  const industries = [
    'TECHNOLOGY', 'HEALTHCARE', 'MANUFACTURING', 'RETAIL', 'FINANCE',
    'EDUCATION', 'CONSULTING', 'REAL_ESTATE', 'TRANSPORTATION', 'ENERGY', 'OTHER'
  ];

  const companyTypes = [
    { value: 'CLIENT', label: 'Client', color: 'bg-blue-100 text-blue-800' },
    { value: 'SUPPLIER', label: 'Supplier', color: 'bg-green-100 text-green-800' },
    { value: 'PROSPECT', label: 'Prospect', color: 'bg-yellow-100 text-yellow-800' }
  ];

  const priorities = [
    { value: 'HIGH', label: 'High', color: 'bg-red-100 text-red-800' },
    { value: 'MEDIUM', label: 'Medium', color: 'bg-yellow-100 text-yellow-800' },
    { value: 'LOW', label: 'Low', color: 'bg-green-100 text-green-800' }
  ];

  useEffect(() => {
    fetchCompanies();
  }, []);

  useEffect(() => {
    filterCompanies();
  }, [companies, searchTerm, selectedType]);

  const fetchCompanies = async () => {
    setIsLoading(true);
    try {
      const token = localStorage.getItem('finsight_token');
      const response = await fetch('http://localhost:8080/api/companies', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (response.ok) {
        const data = await response.json();
        setCompanies(data);
      } else {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to fetch companies');
      }
    } catch (error) {
      console.error('Error fetching companies:', error);
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : 'Failed to fetch companies',
        variant: "destructive",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const filterCompanies = () => {
    let filtered = companies;

    if (searchTerm) {
      filtered = filtered.filter(company =>
        company.companyName.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (selectedType !== 'ALL') {
      filtered = filtered.filter(company => company.type === selectedType);
    }

    setFilteredCompanies(filtered);
  };

  const handleCreateCompany = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsCreating(true);

    try {
      const token = localStorage.getItem('finsight_token');
      const response = await fetch('http://localhost:8080/api/companies', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(newCompany),
      });

      if (response.ok) {
        const createdCompany = await response.json();
        setCompanies(prev => [createdCompany, ...prev]);
        onCompanySelect(createdCompany);
        
        // Reset form
        setNewCompany({
          companyName: '',
          type: 'CLIENT',
          industry: 'TECHNOLOGY',
          status: 'ACTIVE',
          priority: 'MEDIUM'
        });

        toast({
          title: "Company Created",
          description: `${createdCompany.companyName} has been created successfully`,
        });
      } else {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to create company');
      }
    } catch (error) {
      console.error('Error creating company:', error);
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : 'Failed to create company',
        variant: "destructive",
      });
    } finally {
      setIsCreating(false);
    }
  };

  const getTypeColor = (type: string) => {
    const typeConfig = companyTypes.find(t => t.value === type);
    return typeConfig?.color || 'bg-gray-100 text-gray-800';
  };

  const getPriorityColor = (priority: string) => {
    const priorityConfig = priorities.find(p => p.value === priority);
    return priorityConfig?.color || 'bg-gray-100 text-gray-800';
  };

  return (
    <div className="space-y-6">
      <Tabs defaultValue="select" className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="select">Select Company</TabsTrigger>
          <TabsTrigger value="create">Create New</TabsTrigger>
        </TabsList>

        <TabsContent value="select" className="space-y-4">
          {/* Search and Filter */}
          <div className="flex space-x-4">
            <div className="flex-1">
              <Label htmlFor="search">Search Companies</Label>
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="search"
                  placeholder="Search by company name..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <div className="w-48">
              <Label htmlFor="type-filter">Filter by Type</Label>
              <Select value={selectedType} onValueChange={setSelectedType}>
                <SelectTrigger>
                  <SelectValue placeholder="All Types" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Types</SelectItem>
                  {companyTypes.map(type => (
                    <SelectItem key={type.value} value={type.value}>
                      {type.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Companies List */}
          <div className="space-y-3">
            {isLoading ? (
              <div className="text-center py-8">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
                <p className="text-muted-foreground mt-2">Loading companies...</p>
              </div>
            ) : filteredCompanies.length === 0 ? (
              <div className="text-center py-8">
                <Building2 className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">No companies found</p>
              </div>
            ) : (
              filteredCompanies.map((company) => (
                <Card 
                  key={company.id} 
                  className={`cursor-pointer transition-colors hover:bg-muted/50 ${
                    selectedCompany?.id === company.id ? 'ring-2 ring-primary' : ''
                  }`}
                  onClick={() => onCompanySelect(company)}
                >
                  <CardContent className="pt-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          <h3 className="font-semibold">{company.companyName}</h3>
                          {selectedCompany?.id === company.id && (
                            <CheckCircle className="h-5 w-5 text-green-600" />
                          )}
                        </div>
                        <div className="flex items-center space-x-2 mt-2">
                          <Badge className={getTypeColor(company.type)}>
                            {company.type}
                          </Badge>
                          <Badge variant="outline">{company.industry}</Badge>
                          <Badge className={getPriorityColor(company.priority)}>
                            {company.priority}
                          </Badge>
                        </div>
                      </div>
                      <div className="text-right text-sm text-muted-foreground">
                        <p>Created {new Date(company.createdAt).toLocaleDateString()}</p>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))
            )}
          </div>
        </TabsContent>

        <TabsContent value="create" className="space-y-4">
          <form onSubmit={handleCreateCompany} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="companyName">Company Name *</Label>
                <Input
                  id="companyName"
                  value={newCompany.companyName}
                  onChange={(e) => setNewCompany(prev => ({ ...prev, companyName: e.target.value }))}
                  placeholder="Enter company name"
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="type">Type *</Label>
                <Select 
                  value={newCompany.type} 
                  onValueChange={(value: 'CLIENT' | 'SUPPLIER' | 'PROSPECT') => 
                    setNewCompany(prev => ({ ...prev, type: value }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {companyTypes.map(type => (
                      <SelectItem key={type.value} value={type.value}>
                        {type.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="industry">Industry *</Label>
                <Select 
                  value={newCompany.industry} 
                  onValueChange={(value) => 
                    setNewCompany(prev => ({ ...prev, industry: value }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {industries.map(industry => (
                      <SelectItem key={industry} value={industry}>
                        {industry.replace('_', ' ')}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="priority">Priority</Label>
                <Select 
                  value={newCompany.priority} 
                  onValueChange={(value: 'HIGH' | 'MEDIUM' | 'LOW') => 
                    setNewCompany(prev => ({ ...prev, priority: value }))
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {priorities.map(priority => (
                      <SelectItem key={priority.value} value={priority.value}>
                        {priority.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <Button 
              type="submit" 
              disabled={isCreating || !newCompany.companyName.trim()}
              className="w-full"
            >
              {isCreating ? (
                <>
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Creating Company...
                </>
              ) : (
                <>
                  <Plus className="h-4 w-4 mr-2" />
                  Create Company
                </>
              )}
            </Button>
          </form>
        </TabsContent>
      </Tabs>

      {/* Selected Company Summary */}
      {selectedCompany && (
        <Card className="bg-primary/5 border-primary/20">
          <CardContent className="pt-4">
            <div className="flex items-center space-x-3">
              <CheckCircle className="h-5 w-5 text-green-600" />
              <div>
                <h4 className="font-semibold text-green-800">Company Selected</h4>
                <p className="text-sm text-muted-foreground">
                  {selectedCompany.companyName} - {selectedCompany.type}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default CompanyManagement;
