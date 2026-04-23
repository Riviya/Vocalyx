import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';
import { Target, User, Mail, Lock } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const { register, isLoading } = useAuth();
  const { toast } = useToast();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (formData.password !== formData.confirmPassword) {
      toast({
        variant: "destructive",
        title: "Password mismatch",
        description: "Please ensure both passwords match.",
      });
      return;
    }

    if (formData.password.length < 6) {
      toast({
        variant: "destructive",
        title: "Password too short",
        description: "Password must be at least 6 characters long.",
      });
      return;
    }

    try {
      await register({
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
      });
      
      toast({
        title: "Account created!",
        description: "Welcome to Vocalyx. You've been automatically logged in.",
      });
    } catch (error) {
      toast({
        variant: "destructive",
        title: "Registration failed",
        description: "Email may already be in use or server error occurred.",
      });
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary/5 via-background to-primary/10 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl">
        {/* Brand Header */}
        <div className="text-center mb-10">
          <div className="flex items-center justify-center mb-6">
            <div className="bg-gradient-to-r from-primary to-primary-glow p-4 shadow-elegant">
              <Target className="h-10 w-10 text-white" />
            </div>
          </div>
          <h1 className="text-4xl font-bold text-foreground mb-2">Vocalyx</h1>
          <p className="text-lg text-muted-foreground">Join the AI-powered Sales Executive Platform</p>
        </div>

        {/* Register Card */}
        <Card className="shadow-elegant border-0 bg-card/80 backdrop-blur-sm">
          <CardHeader className="text-center pb-8">
            <CardTitle className="text-3xl font-bold text-foreground mb-2">Create Account</CardTitle>
            <CardDescription className="text-base">
              Get started with your AI-powered sales dashboard
            </CardDescription>
          </CardHeader>
          <CardContent className="px-8 pb-8">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-3">
                  <Label htmlFor="firstName" className="text-sm font-semibold text-foreground">First Name</Label>
                  <div className="relative">
                     <User className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                     <Input
                       id="firstName"
                       name="firstName"
                       type="text"
                       placeholder="Enter your first name"
                       value={formData.firstName}
                       onChange={handleChange}
                       className="pl-12 h-12 border-2 border-input focus:border-primary rounded-none bg-background/50"
                       required
                     />
                  </div>
                </div>
                
                <div className="space-y-3">
                  <Label htmlFor="lastName" className="text-sm font-semibold text-foreground">Last Name</Label>
                   <Input
                     id="lastName"
                     name="lastName"
                     type="text"
                     placeholder="Enter your last name"
                     value={formData.lastName}
                     onChange={handleChange}
                     className="h-12 border-2 border-input focus:border-primary rounded-none bg-background/50"
                     required
                   />
                </div>
              </div>

              <div className="space-y-3">
                <Label htmlFor="email" className="text-sm font-semibold text-foreground">Email Address</Label>
                <div className="relative">
                   <Mail className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                   <Input
                     id="email"
                     name="email"
                     type="email"
                     placeholder="Enter your email address"
                     value={formData.email}
                     onChange={handleChange}
                     className="pl-12 h-12 border-2 border-input focus:border-primary rounded-none bg-background/50"
                     required
                   />
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-3">
                  <Label htmlFor="password" className="text-sm font-semibold text-foreground">Password</Label>
                  <div className="relative">
                     <Lock className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                     <Input
                       id="password"
                       name="password"
                       type="password"
                       placeholder="Create a password"
                       value={formData.password}
                       onChange={handleChange}
                       className="pl-12 h-12 border-2 border-input focus:border-primary rounded-none bg-background/50"
                       required
                     />
                  </div>
                </div>

                <div className="space-y-3">
                  <Label htmlFor="confirmPassword" className="text-sm font-semibold text-foreground">Confirm Password</Label>
                  <div className="relative">
                     <Lock className="absolute left-4 top-1/2 transform -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                     <Input
                       id="confirmPassword"
                       name="confirmPassword"
                       type="password"
                       placeholder="Confirm your password"
                       value={formData.confirmPassword}
                       onChange={handleChange}
                       className="pl-12 h-12 border-2 border-input focus:border-primary rounded-none bg-background/50"
                       required
                     />
                  </div>
                </div>
              </div>

               <Button
                 type="submit"
                 className="w-full h-12 text-base font-semibold bg-gradient-to-r from-primary to-primary-glow hover:opacity-90 rounded-none"
                 disabled={isLoading}
               >
                {isLoading ? 'Creating Account...' : 'Create Account'}
              </Button>
            </form>

            <div className="mt-8 text-center">
              <p className="text-sm text-muted-foreground">
                Already have an account?{' '}
                <Link 
                  to="/" 
                  className="text-primary hover:text-primary-glow font-semibold transition-smooth underline"
                >
                  Sign In
                </Link>
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default Register;