import React, { useEffect, useMemo, useState } from 'react';
import axios, { AxiosInstance } from 'axios';
import { useAuth } from '@/contexts/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { 
  DollarSign, 
  TrendingUp, 
  Users, 
  Target,
  BarChart3,
  Activity,
  Calendar,
  CheckCircle,
  Clock,
  AlertTriangle
} from 'lucide-react';
import { Calendar as CalendarComponent } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';
import type { DateRange } from 'react-day-picker';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

type Goal = {
  id: number;
  name: string;
  description: string;
  targetRevenue: number;
  currentProgress: number;
  startDate: string;
  endDate: string;
  status?: string;
  progressPercentage?: number;
  company?: string;
  priority?: string;
};

type SalesLog = {
  id: number;
  customerName: string;
  companyDetails: string;
  saleAmount: number;
  closedDate: string;
  callId?: number;
  callTitle?: string;
  orderId?: number;
  orderNumber?: string;
  contactId?: number;
  contactName?: string;
  userId: number;
  userName: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
};

type DashboardSummary = {
  totalGoals: number;
  achievedGoals: number;
  pendingGoals: number;
  averageProgress: number;
};

type ComprehensiveAnalyticsData = {
  goals: Goal[];
  salesLogs: SalesLog[];
  goalsSummary: DashboardSummary | null;
  salesSummary: {
    totalRevenue: number;
    totalSales: number;
    averageSaleAmount: number;
    topCustomer: string;
  };
};

const ComprehensiveAnalytics: React.FC = () => {
  const { token, user } = useAuth();

  const api: AxiosInstance = useMemo(() => {
    const instance = axios.create({ baseURL: 'http://localhost:8080' });
    instance.interceptors.request.use((config) => {
      if (token) {
        if (!config.headers) config.headers = {} as any;
        (config.headers as any)["Authorization"] = `Bearer ${token}`;
      }
      return config;
    });
    return instance;
  }, [token]);

  const [data, setData] = useState<ComprehensiveAnalyticsData>({
    goals: [],
    salesLogs: [],
    goalsSummary: null,
    salesSummary: {
      totalRevenue: 0,
      totalSales: 0,
      averageSaleAmount: 0,
      topCustomer: 'N/A'
    }
  });
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [dateRange, setDateRange] = useState<DateRange | undefined>(undefined);

  const formatDateInputValue = (isoOrDate: string | Date) => {
    const date = typeof isoOrDate === 'string' ? new Date(isoOrDate) : isoOrDate;
    if (Number.isNaN(date.getTime())) return '';
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const loadComprehensiveData = async () => {
    if (!user?.id) return;
    
    setLoading(true);
    setError(null);
    try {
      // Load goals data
      const [goalsRes, goalsSummaryRes] = await Promise.all([
        api.get<Goal[]>('/api/goals'),
        api.get<DashboardSummary>('/api/goals/dashboard/summary'),
      ]);

      // Load sales data
      let salesData: SalesLog[] = [];
      if (dateRange?.from && dateRange?.to) {
        const startDate = formatDateInputValue(dateRange.from);
        const endDate = formatDateInputValue(dateRange.to);
        const salesRes = await api.get<SalesLog[]>(`/api/sales-log/user/${user.id}/date-range`, {
          params: { startDate, endDate }
        });
        salesData = salesRes.data || [];
      } else {
        const salesRes = await api.get<SalesLog[]>(`/api/sales-log/user/${user.id}`);
        salesData = salesRes.data || [];
      }

      // Calculate sales summary
      const totalRevenue = salesData.reduce((sum, log) => sum + (log.saleAmount || 0), 0);
      const totalSales = salesData.length;
      const averageSaleAmount = totalSales > 0 ? totalRevenue / totalSales : 0;
      
      // Find top customer by revenue
      const customerRevenue = salesData.reduce((acc, log) => {
        const customer = log.customerName || 'Unknown';
        acc[customer] = (acc[customer] || 0) + (log.saleAmount || 0);
        return acc;
      }, {} as Record<string, number>);
      
      const topCustomer = Object.entries(customerRevenue)
        .sort(([,a], [,b]) => b - a)[0]?.[0] || 'N/A';

      setData({
        goals: goalsRes.data || [],
        salesLogs: salesData,
        goalsSummary: goalsSummaryRes.data || null,
        salesSummary: {
          totalRevenue,
          totalSales,
          averageSaleAmount,
          topCustomer
        }
      });
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || 'Failed to load analytics data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (token && user?.id) {
      loadComprehensiveData();
    }
  }, [token, user?.id, dateRange]);

  const currencyUSD = new Intl.NumberFormat('en-US', { 
    style: 'currency', 
    currency: 'USD', 
    maximumFractionDigits: 0 
  });

  const getGoalStatusInfo = (goal: Goal) => {
    const today = new Date();
    const startDate = new Date(goal.startDate);
    const endDate = new Date(goal.endDate);
    
    if (today < startDate) {
      return { status: 'Not Started', color: 'bg-gray-100 text-gray-700', icon: Clock };
    } else if (today > endDate) {
      return { status: 'Date Over', color: 'bg-red-100 text-red-700', icon: AlertTriangle };
    } else if (goal.currentProgress >= goal.targetRevenue) {
      return { status: 'Completed', color: 'bg-green-100 text-green-700', icon: CheckCircle };
    } else {
      return { status: 'In Progress', color: 'bg-blue-100 text-blue-700', icon: Activity };
    }
  };

  const recentSales = useMemo(() => {
    return data.salesLogs
      .sort((a, b) => new Date(b.closedDate).getTime() - new Date(a.closedDate).getTime())
      .slice(0, 5);
  }, [data.salesLogs]);

  const activeGoals = useMemo(() => {
    return data.goals.filter(goal => {
      const today = new Date();
      const startDate = new Date(goal.startDate);
      const endDate = new Date(goal.endDate);
      return today >= startDate && today <= endDate && goal.currentProgress < goal.targetRevenue;
    });
  }, [data.goals]);

  return (
    <div className="space-y-6">
      {/* Date Range Filter */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Comprehensive Analytics</h2>
        <div className="flex items-center gap-2">
          <Popover>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                className={cn(
                  'w-[280px] justify-start text-left font-normal',
                  !dateRange?.from && 'text-muted-foreground'
                )}
              >
                <Calendar className="mr-2 h-4 w-4" />
                {dateRange?.from && dateRange?.to
                  ? `${formatDateInputValue(dateRange.from)} → ${formatDateInputValue(dateRange.to)}`
                  : 'All time'}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0" align="start">
              <CalendarComponent
                mode="range"
                numberOfMonths={2}
                selected={dateRange}
                onSelect={setDateRange}
                initialFocus
              />
            </PopoverContent>
          </Popover>
          
          <Button 
            variant="outline" 
            onClick={() => setDateRange(undefined)}
          >
            Reset
          </Button>
        </div>
      </div>

      {/* Key Metrics Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Target className="h-4 w-4" /> Active Goals
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{activeGoals.length}</div>
            <div className="text-sm text-muted-foreground">
              of {data.goalsSummary?.totalGoals || 0} total
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <DollarSign className="h-4 w-4" /> Total Revenue
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {currencyUSD.format(data.salesSummary.totalRevenue)}
            </div>
            <div className="text-sm text-muted-foreground">
              from {data.salesSummary.totalSales} sales
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <BarChart3 className="h-4 w-4" /> Avg Sale
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {currencyUSD.format(data.salesSummary.averageSaleAmount)}
            </div>
            <div className="text-sm text-muted-foreground">
              per transaction
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Users className="h-4 w-4" /> Top Customer
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-lg font-bold truncate">{data.salesSummary.topCustomer}</div>
            <div className="text-sm text-muted-foreground">
              by revenue
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Goals Progress Overview */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Target className="h-5 w-5" />
            Goals Progress Overview
          </CardTitle>
        </CardHeader>
        <CardContent>
          {data.goalsSummary ? (
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">
                    {data.goalsSummary.achievedGoals}
                  </div>
                  <div className="text-sm text-muted-foreground">Achieved</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">
                    {data.goalsSummary.pendingGoals}
                  </div>
                  <div className="text-sm text-muted-foreground">Pending</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-amber-600">
                    {data.goalsSummary.averageProgress}%
                  </div>
                  <div className="text-sm text-muted-foreground">Avg Progress</div>
                </div>
              </div>
              <Progress value={data.goalsSummary.averageProgress} className="h-2" />
            </div>
          ) : (
            <div className="text-sm text-muted-foreground">No goals data available</div>
          )}
        </CardContent>
      </Card>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Sales */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5" />
              Recent Sales
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {recentSales.length === 0 ? (
                <div className="text-sm text-muted-foreground">No recent sales</div>
              ) : (
                recentSales.map((sale) => (
                  <div key={sale.id} className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
                    <div>
                      <div className="font-medium">{sale.customerName}</div>
                      <div className="text-sm text-muted-foreground">
                        {new Date(sale.closedDate).toLocaleDateString()}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="font-bold">{currencyUSD.format(sale.saleAmount)}</div>
                      {sale.callTitle && (
                        <Badge variant="secondary" className="text-xs">
                          {sale.callTitle}
                        </Badge>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </CardContent>
        </Card>

        {/* Active Goals */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5" />
              Active Goals
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {activeGoals.length === 0 ? (
                <div className="text-sm text-muted-foreground">No active goals</div>
              ) : (
                activeGoals.slice(0, 5).map((goal) => {
                  const statusInfo = getGoalStatusInfo(goal);
                  const StatusIcon = statusInfo.icon;
                  const progressPercentage = Math.min(100, Math.round(((goal.currentProgress || 0) / (goal.targetRevenue || 1)) * 100));
                  
                  return (
                    <div key={goal.id} className="p-3 bg-muted/50 rounded-lg">
                      <div className="flex items-center justify-between mb-2">
                        <div className="font-medium truncate">{goal.name}</div>
                        <Badge className={statusInfo.color}>
                          <StatusIcon className="w-3 h-3 mr-1" />
                          {statusInfo.status}
                        </Badge>
                      </div>
                      <div className="space-y-1">
                        <div className="flex justify-between text-sm">
                          <span>{currencyUSD.format(goal.currentProgress)}</span>
                          <span>{currencyUSD.format(goal.targetRevenue)}</span>
                        </div>
                        <Progress value={progressPercentage} className="h-1" />
                        <div className="text-xs text-muted-foreground text-center">
                          {progressPercentage}% complete
                        </div>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {error && (
        <div className="text-sm text-red-600 bg-red-50 p-3 rounded-md">
          {error}
        </div>
      )}
    </div>
  );
};

export default ComprehensiveAnalytics;

