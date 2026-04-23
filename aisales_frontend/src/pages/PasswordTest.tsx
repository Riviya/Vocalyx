import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useToast } from '@/hooks/use-toast';

const PasswordTest = () => {
  const [email, setEmail] = useState('e.heshananjana@gmail.com');
  const [password, setPassword] = useState('sakith#123');
  const [result, setResult] = useState('');
  const { toast } = useToast();

  const testPassword = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/users/test-password', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      const result = await response.text();
      setResult(result);
      console.log('Test result:', result);
    } catch (error) {
      console.error('Test error:', error);
      toast({
        variant: "destructive",
        title: "Test failed",
        description: "Could not test password",
      });
    }
  };

  return (
    <div className="min-h-screen bg-background p-6">
      <div className="max-w-2xl mx-auto">
        <Card>
          <CardHeader>
            <CardTitle>Password Test (Debug)</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="test-email">Email</Label>
              <Input
                id="test-email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="test-password">Password</Label>
              <Input
                id="test-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <Button onClick={testPassword}>Test Password</Button>
            {result && (
              <div className="p-4 bg-muted rounded">
                <pre className="text-sm">{result}</pre>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default PasswordTest;


