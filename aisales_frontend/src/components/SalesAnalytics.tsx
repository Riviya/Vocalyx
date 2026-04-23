import React, { useEffect, useMemo, useState } from 'react';
import axios, { AxiosInstance } from 'axios';
import { useAuth } from '@/contexts/AuthContext';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { toast } from '@/components/ui/sonner';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { 
  DollarSign, 
  TrendingUp, 
  Users, 
  Calendar, 
  Target,
  BarChart3,
  PieChart,
  Activity
} from 'lucide-react';
import { Calendar as CalendarComponent } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { cn } from '@/lib/utils';
import type { DateRange } from 'react-day-picker';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

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

type SalesAnalyticsSummary = {
  totalRevenue: number;
  totalSales: number;
  averageSaleAmount: number;
  topCustomer: string;
  revenueGrowth: number;
};

const SalesAnalytics: React.FC = () => {
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

  const [salesLogs, setSalesLogs] = useState<SalesLog[]>([]);
  const [summary, setSummary] = useState<SalesAnalyticsSummary | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const [dateRange, setDateRange] = useState<DateRange | undefined>(undefined);
  const [timeFilter, setTimeFilter] = useState<string>('all');

  const formatDateInputValue = (isoOrDate: string | Date) => {
    const date = typeof isoOrDate === 'string' ? new Date(isoOrDate) : isoOrDate;
    if (Number.isNaN(date.getTime())) return '';
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const loadSalesData = async () => {
    if (!user?.id) return;
    
    setLoading(true);
    setError(null);
    try {
      let salesData: SalesLog[] = [];
      
      if (dateRange?.from && dateRange?.to) {
        const startDate = formatDateInputValue(dateRange.from);
        const endDate = formatDateInputValue(dateRange.to);
        const response = await api.get<SalesLog[]>(`/api/sales-log/user/${user.id}/date-range`, {
          params: { startDate, endDate }
        });
        salesData = response.data || [];
      } else {
        const response = await api.get<SalesLog[]>(`/api/sales-log/user/${user.id}`);
        salesData = response.data || [];
      }

      setSalesLogs(salesData);
      
      // Calculate summary
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

      setSummary({
        totalRevenue,
        totalSales,
        averageSaleAmount,
        topCustomer,
        revenueGrowth: 0 // TODO: Calculate growth compared to previous period
      });
    } catch (e: any) {
      setError(e?.response?.data?.message || e?.message || 'Failed to load sales data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (token && user?.id) {
      loadSalesData();
    }
  }, [token, user?.id, dateRange]);

  const currencyUSD = new Intl.NumberFormat('en-US', { 
    style: 'currency', 
    currency: 'USD', 
    maximumFractionDigits: 0 
  });

  const filteredSalesLogs = useMemo(() => {
    return salesLogs.sort((a, b) => 
      new Date(b.closedDate).getTime() - new Date(a.closedDate).getTime()
    );
  }, [salesLogs]);

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <DollarSign className="h-4 w-4" /> Total Revenue
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {summary ? currencyUSD.format(summary.totalRevenue) : '$0'}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <TrendingUp className="h-4 w-4" /> Total Sales
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">{summary?.totalSales || 0}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <BarChart3 className="h-4 w-4" /> Average Sale
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-3xl font-bold">
              {summary ? currencyUSD.format(summary.averageSaleAmount) : '$0'}
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
            <div className="text-lg font-bold truncate">{summary?.topCustomer || 'N/A'}</div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-semibold">Sales Analytics</h2>
        <div className="flex items-center gap-2">
          <Select value={timeFilter} onValueChange={setTimeFilter}>
            <SelectTrigger className="w-[140px]">
              <SelectValue placeholder="Time Period" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Time</SelectItem>
              <SelectItem value="custom">Custom Range</SelectItem>
            </SelectContent>
          </Select>
          
          {timeFilter === 'custom' && (
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
                    : 'Pick a date range'}
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
          )}
          
          <Button 
            variant="outline" 
            onClick={() => {
              setTimeFilter('all');
              setDateRange(undefined);
            }}
          >
            Reset
          </Button>
        </div>
      </div>

      {/* Sales Logs Table */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Customer</TableHead>
              <TableHead>Company</TableHead>
              <TableHead>Sale Amount</TableHead>
              <TableHead>Closed Date</TableHead>
              <TableHead>Call/Order</TableHead>
              <TableHead>Notes</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredSalesLogs.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="text-center text-muted-foreground">
                  {loading ? 'Loading sales data...' : 'No sales data found.'}
                </TableCell>
              </TableRow>
            ) : (
              filteredSalesLogs.map((log) => (
                <TableRow key={log.id}>
                  <TableCell className="font-medium">{log.customerName}</TableCell>
                  <TableCell>{log.companyDetails || '-'}</TableCell>
                  <TableCell className="font-medium">
                    {currencyUSD.format(log.saleAmount || 0)}
                  </TableCell>
                  <TableCell>
                    {new Date(log.closedDate).toLocaleDateString()}
                  </TableCell>
                  <TableCell>
                    {log.callTitle && (
                      <Badge variant="secondary" className="mr-1">
                        Call: {log.callTitle}
                      </Badge>
                    )}
                    {log.orderNumber && (
                      <Badge variant="outline">
                        Order: {log.orderNumber}
                      </Badge>
                    )}
                    {!log.callTitle && !log.orderNumber && '-'}
                  </TableCell>
                  <TableCell className="max-w-xs truncate">
                    {log.notes || '-'}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {error && (
        <div className="text-sm text-red-600 bg-red-50 p-3 rounded-md">
          {error}
        </div>
      )}
    </div>
  );
};

export default SalesAnalytics;
