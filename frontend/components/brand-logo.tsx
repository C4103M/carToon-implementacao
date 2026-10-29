import React from "react"
import Image from "next/image"
import Link from "next/link"
import { cn } from "@/lib/utils"

export interface BrandLogoProps {
  variant?: "on-dark" | "on-light" | "auto"
  size?: "xs" | "sm" | "md" | "lg" | "xl"
  className?: string
  href?: string
  priority?: boolean
}

const sizeMap = {
  xs: { height: 24, width: 57, className: "h-6 w-auto" },
  sm: { height: 32, width: 75, className: "h-8 w-auto" },
  md: { height: 40, width: 94, className: "h-10 w-auto" },
  lg: { height: 48, width: 113, className: "h-12 w-auto" },
  xl: { height: 64, width: 151, className: "h-16 w-auto" },
}

export function BrandLogo({
  variant = "auto",
  size = "md",
  className,
  href,
  priority = false,
}: BrandLogoProps) {
  const config = sizeMap[size]

  const content = (
    <div
      className={cn(
        "relative inline-flex items-center justify-center select-none shrink-0",
        className
      )}
    >
      {variant === "on-dark" ? (
        <Image
          src="/logo-cartoon-dark.png"
          alt="CAR TOON"
          width={config.width}
          height={config.height}
          priority={priority}
          className={cn("object-contain", config.className)}
        />
      ) : variant === "on-light" ? (
        <Image
          src="/logo-cartoon.png"
          alt="CAR TOON"
          width={config.width}
          height={config.height}
          priority={priority}
          className={cn("object-contain", config.className)}
        />
      ) : (
        <>
          <Image
            src="/logo-cartoon.png"
            alt="CAR TOON"
            width={config.width}
            height={config.height}
            priority={priority}
            className={cn("object-contain dark:hidden", config.className)}
          />
          <Image
            src="/logo-cartoon-dark.png"
            alt="CAR TOON"
            width={config.width}
            height={config.height}
            priority={priority}
            className={cn("object-contain hidden dark:inline-block", config.className)}
          />
        </>
      )}
    </div>
  )

  if (href) {
    return (
      <Link
        href={href}
        className="inline-flex items-center focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary rounded-lg transition-transform hover:scale-[1.02] active:scale-[0.98]"
      >
        {content}
      </Link>
    )
  }

  return content
}

