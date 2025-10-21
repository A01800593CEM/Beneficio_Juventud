// ============================================================================
// COMPONENT: PromotionKPIs - Indicadores clave de rendimiento
// ============================================================================

import KPICard from '@/features/admin/components/KPICard';

interface PromotionKPIsProps {
  stats: {
    total: number;
    active: number;
    totalStock: number;
    usedStock: number;
  };
}

export default function PromotionKPIs({ stats }: PromotionKPIsProps) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <KPICard
        title="Total Promociones"
        value={stats.total}
        icon={<span className="text-[#008D96]">📊</span>}
      />
      <KPICard
        title="Promociones Activas"
        value={stats.active}
        icon={<span className="text-[#008D96]">✅</span>}
      />
      <KPICard
        title="Cupones Otorgados"
        value={stats.totalStock}
        icon={<span className="text-[#008D96]">📦</span>}
      />
      <KPICard
        title="Cupones Usados"
        value={stats.usedStock}
        icon={<span className="text-[#008D96]">🎯</span>}
      />
    </div>
  );
}