import { Analytics } from '@vercel/analytics/next';
import type { Metadata, Viewport } from 'next';
import { Plus_Jakarta_Sans, Bricolage_Grotesque, JetBrains_Mono } from 'next/font/google';
import { Toaster } from '@/components/ui/sonner';
import { StoreProvider } from '@/lib/store';
import { ThemeProvider } from '@/components/theme-provider';
import './globals.css';

const fontSans = Plus_Jakarta_Sans({
    subsets: ['latin'],
    variable: '--font-sans',
    display: 'swap',
});

const fontDisplay = Bricolage_Grotesque({
    subsets: ['latin'],
    variable: '--font-display',
    display: 'swap',
});

const fontMono = JetBrains_Mono({
    subsets: ['latin'],
    variable: '--font-mono',
    display: 'swap',
});

export const metadata: Metadata = {
    title: 'CAR TOON — Gestão de Oficinas Mecânicas',
    description:
        'Sistema de gestão para oficinas mecânicas: orçamentos inteligentes, tabela FIPE, clientes, veículos e catálogo de serviços.',
    generator: 'v0.app',
    icons: {
        icon: [
            { url: '/logo-cartoon.png', type: 'image/png' },
            { url: '/icon.png', type: 'image/png' },
            { url: '/favicon.ico', sizes: 'any' },
        ],
        shortcut: '/logo-cartoon.png',
        apple: '/apple-icon.png',
    },
};

export const viewport: Viewport = {
    colorScheme: 'light dark',
    themeColor: [
        { media: '(prefers-color-scheme: light)', color: '#F7F2EA' },
        { media: '(prefers-color-scheme: dark)', color: '#0F131C' },
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
            className={`bg-background ${fontSans.variable} ${fontDisplay.variable} ${fontMono.variable}`}
            suppressHydrationWarning
        >
            <body className="font-sans antialiased selection:bg-primary selection:text-primary-foreground">
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
