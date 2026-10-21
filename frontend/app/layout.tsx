import { Analytics } from '@vercel/analytics/next';
import type { Metadata, Viewport } from 'next';
import { Geist, Geist_Mono } from 'next/font/google';
import { Toaster } from '@/components/ui/sonner';
import { StoreProvider } from '@/lib/store';
import { ThemeProvider } from '@/components/theme-provider';
import './globals.css';

const geistSans = Geist({ subsets: ['latin'], variable: '--font-geist-sans' });
const geistMono = Geist_Mono({
    subsets: ['latin'],
    variable: '--font-geist-mono',
});

export const metadata: Metadata = {
    title: 'OficinaPro — Gestão para Oficinas Mecânicas',
    description:
        'Sistema de gestão para oficinas mecânicas: clientes, veículos, catálogo de serviços e orçamentos.',
    generator: 'v0.app',
};

export const viewport: Viewport = {
    colorScheme: 'light dark',
    themeColor: [
        { media: '(prefers-color-scheme: light)', color: '#f7f5f0' },
        { media: '(prefers-color-scheme: dark)', color: '#252220' },
    ],
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {   
    return (
        <html
            lang="pt-BR"
            className={`bg-background ${geistSans.variable} ${geistMono.variable}`}
            suppressHydrationWarning
        >
            <body className="font-sans antialiased">
                <ThemeProvider
                    attribute="class"
                    defaultTheme="system"
                    enableSystem
                    disableTransitionOnChange
                >
                    <StoreProvider>{children}</StoreProvider>
                    <Toaster richColors position="top-right" />
                    {process.env.NODE_ENV === 'production' && <Analytics />}
                </ThemeProvider>
            </body>
        </html>
    );
}
