import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';

const WorkspaceSetup = () => {
  const [companyName, setCompanyName] = useState('');
  const [industry, setIndustry] = useState('');
  const [address, setAddress] = useState('');
  const { token, refreshUser } = useAuth();
  const { toast } = useToast();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/api/users/create-company', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({ companyName, industry, address }),
      });

      if (!response.ok) {
        throw new Error('Failed to create company');
      }

      const userData = await response.json();
      
      // Update the user data in localStorage and context
      localStorage.setItem('finsight_user', JSON.stringify(userData));
      refreshUser();
      
      toast({ title: 'Company created', description: 'Your company has been set up successfully.' });
      navigate('/dashboard');
    } catch (err) {
      toast({ variant: 'destructive', title: 'Creation failed', description: 'Please try again.' });
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-xl">
        <Card className="border-0">
          <CardHeader>
            <CardTitle>Company Setup</CardTitle>
            <CardDescription>Create your company to get started with Vocalyx</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="companyName">Company Name</Label>
                <Input id="companyName" value={companyName} onChange={(e) => setCompanyName(e.target.value)} required />
              </div>
              <div className="space-y-2">
                <Label htmlFor="industry">Industry</Label>
                <Input id="industry" value={industry} onChange={(e) => setIndustry(e.target.value)} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="address">Address</Label>
                <Input id="address" value={address} onChange={(e) => setAddress(e.target.value)} />
              </div>
              <Button type="submit" className="w-full">Create Company</Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default WorkspaceSetup;


