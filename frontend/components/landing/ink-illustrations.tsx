import React from 'react';

export const SquiggleUnderline: React.FC<{ className?: string }> = ({
  className = 'w-full h-3.5 text-coral',
}) => (
  <svg
    viewBox="0 0 260 16"
    fill="none"
    preserveAspectRatio="none"
    aria-hidden="true"
    className={className}
  >
    <path
      d="M4 11.5C52 3.5 104 14.5 156 6.5C194 1.5 228 11.5 256 7"
      stroke="currentColor"
      strokeWidth="4.5"
      strokeLinecap="round"
      strokeLinejoin="round"
      className="squiggle-draw"
    />
  </svg>
);

export const RoadCarSvg: React.FC<{ className?: string }> = ({
  className = 'h-11 w-auto',
}) => (
  <svg
    viewBox="0 0 120 72"
    fill="none"
    aria-hidden="true"
    className={className}
  >
    {/* Speed lines behind the car */}
    <path
      d="M4 34H18M2 44H14"
      stroke="#D9462F"
      strokeWidth="3.5"
      strokeLinecap="round"
      className="animate-dash-pulse"
    />
    {/* Cartoon Car Body inspired by official CAR TOON silhouette */}
    <path
      d="M24 48C19 48 16 44 16 39C16 34 20 31 24 30C28 15 44 8 64 8C83 8 95 16 99 28L108 31C113 33 116 37 116 43C116 47 112 49 107 49H24Z"
      fill="#D9462F"
      stroke="#1B2A4A"
      strokeWidth="3.5"
      strokeLinejoin="round"
    />
    {/* Cartoon Window Cutout */}
    <path
      d="M40 28C43 18 53 14 66 14C78 14 86 19 89 28H40Z"
      fill="#FCF9F4"
      stroke="#1B2A4A"
      strokeWidth="3"
      strokeLinejoin="round"
    />
    {/* Left Wheel */}
    <circle
      cx="42"
      cy="50"
      r="11"
      fill="#D9462F"
      stroke="#1B2A4A"
      strokeWidth="3.5"
    />
    <circle cx="42" cy="50" r="5" fill="#FCF9F4" stroke="#1B2A4A" strokeWidth="2.5" />
    {/* Right Wheel */}
    <circle
      cx="90"
      cy="50"
      r="11"
      fill="#D9462F"
      stroke="#1B2A4A"
      strokeWidth="3.5"
    />
    <circle cx="90" cy="50" r="5" fill="#FCF9F4" stroke="#1B2A4A" strokeWidth="2.5" />
  </svg>
);

/**
 * Custom Editorial Cartoon-Ink Illustration for the Hero Section:
 * Depicts the CAR TOON car on a hydraulic workshop lift with a clipped OS sheet.
 */
export const HeroGarageIllustration: React.FC<{
  onOpenSimulator: () => void;
}> = ({ onOpenSimulator }) => (
  <div className="relative mx-auto w-full max-w-[540px] select-none">
    <svg
      viewBox="0 0 560 450"
      fill="none"
      role="img"
      aria-label="Ilustração em traço de tinta do carro CAR TOON no elevador da oficina com ficha de Ordem de Serviço"
      className="w-full h-auto overflow-visible"
    >
      {/* Subtle architectural drafting lines */}
      <line
        x1="24"
        y1="392"
        x2="536"
        y2="392"
        stroke="#1B2A4A"
        strokeWidth="3.5"
        strokeLinecap="round"
      />
      <line
        x1="68"
        y1="406"
        x2="492"
        y2="406"
        stroke="#1B2A4A"
        strokeWidth="2"
        strokeDasharray="8 8"
        strokeLinecap="round"
        opacity="0.45"
      />

      {/* Hydraulic Lift Pillars */}
      <rect
        x="118"
        y="220"
        width="22"
        height="172"
        rx="4"
        fill="#EFE7DB"
        stroke="#1B2A4A"
        strokeWidth="3.5"
      />
      <rect
        x="376"
        y="220"
        width="22"
        height="172"
        rx="4"
        fill="#EFE7DB"
        stroke="#1B2A4A"
        strokeWidth="3.5"
      />
      {/* Hydraulic Lift Crossbeam */}
      <rect
        x="82"
        y="206"
        width="352"
        height="16"
        rx="6"
        fill="#1B2A4A"
      />
      <path
        d="M96 206L110 192H144L152 206M364 206L372 192H406L420 206"
        stroke="#1B2A4A"
        strokeWidth="3.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />

      {/* Expressive Cartoon Motion / Speed Lines */}
      <g className="animate-dash-pulse">
        <path
          d="M28 112H68"
          stroke="#D9462F"
          strokeWidth="4"
          strokeLinecap="round"
        />
        <path
          d="M16 134H54"
          stroke="#1B2A4A"
          strokeWidth="3.5"
          strokeLinecap="round"
        />
        <path
          d="M36 154H64"
          stroke="#D9462F"
          strokeWidth="3.5"
          strokeLinecap="round"
        />
      </g>

      {/* Hand-drawn Spark / Ink Marks near roof */}
      <path
        d="M428 46L442 30M448 58L468 50M414 34L418 16"
        stroke="#D9462F"
        strokeWidth="3.5"
        strokeLinecap="round"
      />

      {/* Animated CAR TOON Car on the Lift */}
      <g className="animate-car-idle">
        {/* Shadow under wheels on the beam */}
        <ellipse cx="182" cy="204" rx="34" ry="5" fill="#1B2A4A" opacity="0.25" />
        <ellipse cx="338" cy="204" rx="34" ry="5" fill="#1B2A4A" opacity="0.25" />

        {/* Car Silhouette matching logo dome curve */}
        <path
          d="M106 168C86 168 74 155 74 136C74 118 87 106 104 103C122 48 176 22 252 22C322 22 368 52 384 96L416 105C435 111 446 125 446 146C446 161 433 168 414 168H106Z"
          fill="#D9462F"
          stroke="#1B2A4A"
          strokeWidth="4.5"
          strokeLinejoin="round"
        />

        {/* Bold Hand-Cut White "CAR" Lettering inside the Car Body (Homage to Logo) */}
        <text
          x="152"
          y="134"
          fill="#FCF9F4"
          fontFamily="'Bricolage Grotesque', sans-serif"
          fontWeight="800"
          fontSize="74"
          letterSpacing="-2"
        >
          CAR
        </text>

        {/* Headlight & Bumper Ink Detail */}
        <path
          d="M422 120C430 123 434 130 432 138"
          stroke="#FCF9F4"
          strokeWidth="4"
          strokeLinecap="round"
        />

        {/* Front & Rear Cartoon Wheels */}
        <g>
          <circle
            cx="182"
            cy="172"
            r="34"
            fill="#D9462F"
            stroke="#1B2A4A"
            strokeWidth="4.5"
          />
          <circle
            cx="182"
            cy="172"
            r="16"
            fill="#FCF9F4"
            stroke="#1B2A4A"
            strokeWidth="4"
          />
        </g>
        <g>
          <circle
            cx="338"
            cy="172"
            r="34"
            fill="#D9462F"
            stroke="#1B2A4A"
            strokeWidth="4.5"
          />
          <circle
            cx="338"
            cy="172"
            r="16"
            fill="#FCF9F4"
            stroke="#1B2A4A"
            strokeWidth="4"
          />
        </g>
      </g>

      {/* Drawn Clipboard / Service Order Sheet Overlapping the Lift (Pure SVG Ink Art) */}
      <g
        role="button"
        tabIndex={0}
        aria-label="Abrir simulador interativo da Ordem de Serviço"
        onClick={onOpenSimulator}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onOpenSimulator();
          }
        }}
        className="cursor-pointer transition-transform duration-300 ease-spring hover:-translate-y-1 focus:outline-none"
      >
        {/* Hard Ink Offset Shadow of the Sheet */}
        <rect
          x="216"
          y="246"
          width="310"
          height="182"
          rx="14"
          fill="#1B2A4A"
        />
        {/* Paper Sheet */}
        <rect
          x="208"
          y="238"
          width="310"
          height="182"
          rx="14"
          fill="#FCF9F4"
          stroke="#1B2A4A"
          strokeWidth="3.5"
        />
        {/* Metal Clip at Top of Sheet */}
        <rect
          x="332"
          y="226"
          width="62"
          height="20"
          rx="6"
          fill="#EFE7DB"
          stroke="#1B2A4A"
          strokeWidth="3"
        />

        {/* License Plate Stamp */}
        <rect
          x="228"
          y="260"
          width="98"
          height="32"
          rx="6"
          fill="#F7F2EA"
          stroke="#1B2A4A"
          strokeWidth="2.5"
        />
        <text
          x="240"
          y="281"
          fill="#1B2A4A"
          fontFamily="'JetBrains Mono', monospace"
          fontWeight="700"
          fontSize="15"
        >
          ABC1D23
        </text>

        {/* OS Number & Vehicle */}
        <text
          x="338"
          y="273"
          fill="#B42E18"
          fontFamily="'JetBrains Mono', monospace"
          fontWeight="700"
          fontSize="11"
        >
          OS #1042 · GOL 1.0
        </text>
        <text
          x="338"
          y="289"
          fill="#1B2A4A"
          fontFamily="'Plus Jakarta Sans', sans-serif"
          fontWeight="700"
          fontSize="12"
        >
          FIPE: R$ 48.950
        </text>

        {/* Divider Line */}
        <line
          x1="228"
          y1="306"
          x2="498"
          y2="306"
          stroke="#1B2A4A"
          strokeWidth="1.5"
          strokeDasharray="4 4"
        />

        {/* Order Lines */}
        <text
          x="228"
          y="328"
          fill="#1B2A4A"
          fontFamily="'Plus Jakarta Sans', sans-serif"
          fontWeight="600"
          fontSize="12.5"
        >
          Kit correia dentada + tensor
        </text>
        <text
          x="496"
          y="328"
          textAnchor="end"
          fill="#1B2A4A"
          fontFamily="'JetBrains Mono', monospace"
          fontWeight="700"
          fontSize="12.5"
        >
          R$ 420
        </text>

        <text
          x="228"
          y="350"
          fill="#1B2A4A"
          fontFamily="'Plus Jakarta Sans', sans-serif"
          fontWeight="600"
          fontSize="12.5"
        >
          Mão de obra sincronismo
        </text>
        <text
          x="496"
          y="350"
          textAnchor="end"
          fill="#1B2A4A"
          fontFamily="'JetBrains Mono', monospace"
          fontWeight="700"
          fontSize="12.5"
        >
          R$ 350
        </text>

        <text
          x="228"
          y="372"
          fill="#1B2A4A"
          fontFamily="'Plus Jakarta Sans', sans-serif"
          fontWeight="600"
          fontSize="12.5"
        >
          Óleo sintético 5W30 + filtro
        </text>
        <text
          x="496"
          y="372"
          textAnchor="end"
          fill="#1B2A4A"
          fontFamily="'JetBrains Mono', monospace"
          fontWeight="700"
          fontSize="12.5"
        >
          R$ 310
        </text>

        {/* Bottom Total Bar */}
        <line
          x1="228"
          y1="385"
          x2="498"
          y2="385"
          stroke="#1B2A4A"
          strokeWidth="2"
        />
        <text
          x="228"
          y="405"
          fill="#B42E18"
          fontFamily="'JetBrains Mono', monospace"
          fontWeight="700"
          fontSize="12"
        >
          2,2% DO VALOR FIPE
        </text>
        <text
          x="496"
          y="406"
          textAnchor="end"
          fill="#1B2A4A"
          fontFamily="'Bricolage Grotesque', sans-serif"
          fontWeight="800"
          fontSize="16"
        >
          Total R$ 1.080
        </text>
      </g>
    </svg>
  </div>
);

/**
 * Four Hand-Drawn Ink Sketches for the Editorial Feature List
 */
export const FeatureFipeSketch: React.FC = () => (
  <svg viewBox="0 0 240 120" fill="none" aria-hidden="true" className="h-28 w-auto">
    {/* Scale / Gauge comparing car value vs repair */}
    <path
      d="M24 96H216"
      stroke="#1B2A4A"
      strokeWidth="3"
      strokeLinecap="round"
    />
    {/* FIPE Tag */}
    <rect
      x="28"
      y="22"
      width="110"
      height="54"
      rx="10"
      fill="#FCF9F4"
      stroke="#1B2A4A"
      strokeWidth="3"
    />
    <text
      x="42"
      y="44"
      fill="#B42E18"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="11"
    >
      TABELA FIPE
    </text>
    <text
      x="42"
      y="64"
      fill="#1B2A4A"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="14"
    >
      R$ 48.950
    </text>
    {/* Proportional coral bar */}
    <rect
      x="154"
      y="34"
      width="62"
      height="42"
      rx="10"
      fill="#D9462F"
      stroke="#1B2A4A"
      strokeWidth="3"
    />
    <text
      x="185"
      y="60"
      textAnchor="middle"
      fill="#FFFFFF"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="14"
    >
      2,2%
    </text>
  </svg>
);

export const FeatureMechanicSketch: React.FC = () => (
  <svg viewBox="0 0 240 120" fill="none" aria-hidden="true" className="h-28 w-auto">
    {/* Open-end wrench in cartoon ink */}
    <path
      d="M48 78L122 30C128 18 144 14 156 22L142 36L150 48L166 36C172 48 166 64 152 68L78 106C64 112 48 104 44 92C42 86 44 81 48 78Z"
      fill="#EFE7DB"
      stroke="#1B2A4A"
      strokeWidth="3"
      strokeLinejoin="round"
    />
    {/* Mechanic Bay Tag */}
    <rect
      x="122"
      y="56"
      width="96"
      height="44"
      rx="8"
      fill="#FCF9F4"
      stroke="#1B2A4A"
      strokeWidth="3"
    />
    <circle cx="138" cy="78" r="5" fill="#D9462F" />
    <text
      x="150"
      y="82"
      fill="#1B2A4A"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="12"
    >
      BOX 01 · OS
    </text>
  </svg>
);

export const FeaturePlateSketch: React.FC = () => (
  <svg viewBox="0 0 240 120" fill="none" aria-hidden="true" className="h-28 w-auto">
    {/* Mercosul License Plate in Hand-Drawn Ink */}
    <rect
      x="34"
      y="28"
      width="176"
      height="68"
      rx="10"
      fill="#1B2A4A"
    />
    <rect
      x="30"
      y="24"
      width="176"
      height="68"
      rx="10"
      fill="#FCF9F4"
      stroke="#1B2A4A"
      strokeWidth="3"
    />
    <path
      d="M32 34C32 28.5 36.5 25 42 25H194C199.5 25 204 28.5 204 34V42H32V34Z"
      fill="#1B2A4A"
    />
    <text
      x="118"
      y="37"
      textAnchor="middle"
      fill="#FCF9F4"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="9"
      letterSpacing="3"
    >
      BRASIL
    </text>
    <text
      x="118"
      y="74"
      textAnchor="middle"
      fill="#1B2A4A"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="24"
      letterSpacing="4"
    >
      ABC1D23
    </text>
  </svg>
);

export const FeatureCatalogSketch: React.FC = () => (
  <svg viewBox="0 0 240 120" fill="none" aria-hidden="true" className="h-28 w-auto">
    {/* Catalog Price vs Frozen OS Price */}
    <rect
      x="20"
      y="30"
      width="88"
      height="60"
      rx="8"
      fill="#EFE7DB"
      stroke="#1B2A4A"
      strokeWidth="3"
    />
    <text
      x="32"
      y="52"
      fill="#44516E"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="10"
    >
      CATÁLOGO
    </text>
    <text
      x="32"
      y="74"
      fill="#B42E18"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="13"
    >
      R$ 320
    </text>

    {/* Arrow */}
    <path
      d="M114 60H132"
      stroke="#1B2A4A"
      strokeWidth="3"
      strokeLinecap="round"
    />

    {/* Locked OS Price */}
    <rect
      x="138"
      y="30"
      width="88"
      height="60"
      rx="8"
      fill="#FCF9F4"
      stroke="#1B2A4A"
      strokeWidth="3"
    />
    <text
      x="150"
      y="52"
      fill="#1B2A4A"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="10"
    >
      OS ANTIGA
    </text>
    <text
      x="150"
      y="74"
      fill="#1B2A4A"
      fontFamily="'JetBrains Mono', monospace"
      fontWeight="700"
      fontSize="13"
    >
      R$ 285
    </text>
  </svg>
);

