import React, { useEffect, useMemo, useState } from 'react';
import axios, { AxiosInstance } from 'axios';
import { useAuth } from '@/contexts/AuthContext';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { DollarSign, TrendingUp, Users, BarChart3, Calendar, Loader2 } from 'lucide-react';
import { toast } from '@/components/ui/sonner';

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

const SalesAnalyticsSimple: React.FC = () => {
    const { token, user } = useAuth();
    const [salesData, setSalesData] = useState<{
        totalRevenue: number;
        totalSales: number;
        averageSaleAmount: number;
        topCustomer: string;
        recentSales: SalesLog[];
    }>({
        totalRevenue: 0,
        totalSales: 0,
        averageSaleAmount: 0,
        topCustomer: 'N/A',
        recentSales: []
    });
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

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

    const loadSalesData = async () => {
        if (!user?.id) return;
        
        setLoading(true);
        setError(null);
        try {
            // Update goal progress from sales first
            await api.post('/api/goal-sales/update-progress');
            
            // Get sales data
            const [recentSalesRes, totalRevenueRes, salesCountRes] = await Promise.all([
                api.get<SalesLog[]>('/api/goal-sales/recent-sales'),
                api.get<number>('/api/goal-sales/total-revenue'),
                api.get<number>('/api/goal-sales/total-sales-count')
            ]);
            
            const recentSales = recentSalesRes.data || [];
            const totalRevenue = totalRevenueRes.data || 0;
            const totalSales = salesCountRes.data || 0;
            const averageSaleAmount = totalSales > 0 ? totalRevenue / totalSales : 0;
            
            // Find top customer by revenue
            const customerRevenue = recentSales.reduce((acc, log) => {
                const customer = log.customerName || 'Unknown';
                acc[customer] = (acc[customer] || 0) + (log.saleAmount || 0);
                return acc;
            }, {} as Record<string, number>);
            
            const topCustomer = Object.entries(customerRevenue)
                .sort(([,a], [,b]) => b - a)[0]?.[0] || 'N/A';

            setSalesData({
                totalRevenue,
                totalSales,
                averageSaleAmount,
                topCustomer,
                recentSales: recentSales.slice(0, 10) // Show last 10 sales
            });
        } catch (e: any) {
            const errorMessage = e?.response?.data?.message || e?.message || 'Failed to load sales data';
            setError(errorMessage);
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (token && user?.id) {
            loadSalesData();
        }
    }, [token, user?.id]);

    const currencyUSD = new Intl.NumberFormat('en-US', { 
        style: 'currency', 
        currency: 'USD', 
        maximumFractionDigits: 0 
    });

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
                            {loading ? <Loader2 className="h-8 w-8 animate-spin" /> : currencyUSD.format(salesData.totalRevenue)}
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
                        <div className="text-3xl font-bold">
                            {loading ? <Loader2 className="h-8 w-8 animate-spin" /> : salesData.totalSales}
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
                            {loading ? <Loader2 className="h-8 w-8 animate-spin" /> : currencyUSD.format(salesData.averageSaleAmount)}
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
                        <div className="text-lg font-bold">
                            {loading ? <Loader2 className="h-6 w-6 animate-spin" /> : salesData.topCustomer}
                        </div>
                    </CardContent>
                </Card>
            </div>

            {/* Recent Sales Table */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Calendar className="h-5 w-5" />
                        Recent Sales
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="flex items-center justify-center h-32">
                            <Loader2 className="h-8 w-8 animate-spin" />
                        </div>
                    ) : salesData.recentSales.length === 0 ? (
                        <div className="text-center text-muted-foreground py-8">
                            No sales data found
                        </div>
                    ) : (
                        <div className="rounded-md border">
                            <Table>
                                <TableHeader>
                                    <TableRow>
                                        <TableHead>Customer</TableHead>
                                        <TableHead>Company</TableHead>
                                        <TableHead>Amount</TableHead>
                                        <TableHead>Date</TableHead>
                                        <TableHead>Source</TableHead>
                                    </TableRow>
                                </TableHeader>
                                <TableBody>
                                    {salesData.recentSales.map((sale) => (
                                        <TableRow key={sale.id}>
                                            <TableCell className="font-medium">{sale.customerName}</TableCell>
                                            <TableCell>{sale.companyDetails || '-'}</TableCell>
                                            <TableCell className="font-bold text-green-600">
                                                {currencyUSD.format(sale.saleAmount)}
                                            </TableCell>
                                            <TableCell>
                                                {new Date(sale.closedDate).toLocaleDateString()}
                                            </TableCell>
                                            <TableCell>
                                                {sale.callTitle && (
                                                    <Badge variant="secondary" className="mr-1">
                                                        Call: {sale.callTitle}
                                                    </Badge>
                                                )}
                                                {sale.orderNumber && (
                                                    <Badge variant="outline">
                                                        Order: {sale.orderNumber}
                                                    </Badge>
                                                )}
                                                {!sale.callTitle && !sale.orderNumber && '-'}
                                            </TableCell>
                                        </TableRow>
                                    ))}
                                </TableBody>
                            </Table>
                        </div>
                    )}
                </CardContent>
            </Card>

            {error && (
                <div className="text-sm text-red-600 bg-red-50 p-3 rounded-md">
                    {error}
                </div>
            )}
        </div>
    );
};

export default SalesAnalyticsSimple;
