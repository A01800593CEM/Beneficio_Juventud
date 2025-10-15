import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    console.log('🪝 Webhook received - Promotion event:', body);

    // Validate webhook payload
    if (!body.event || !body.data) {
      return NextResponse.json(
        { error: 'Invalid webhook payload' },
        { status: 400 }
      );
    }

    const { event, data } = body;

    switch (event) {
      case 'promotion.created':
        console.log('✅ New promotion created:', data);
        // Here you could:
        // - Send notifications to collaborators
        // - Update analytics
        // - Trigger marketing campaigns
        // - Send to third-party services
        break;

      case 'promotion.updated':
        console.log('📝 Promotion updated:', data);
        break;

      case 'promotion.deleted':
        console.log('🗑️ Promotion deleted:', data);
        break;

      case 'promotion.redeemed':
        console.log('🎉 Promotion redeemed:', data);
        break;

      default:
        console.log('⚠️ Unknown promotion event:', event);
    }

    // Acknowledge the webhook
    return NextResponse.json({
      success: true,
      message: `Webhook processed: ${event}`,
      timestamp: new Date().toISOString()
    });

  } catch (error) {
    console.error('💥 Webhook processing error:', error);

    return NextResponse.json(
      {
        error: 'Webhook processing failed',
        details: error instanceof Error ? error.message : 'Unknown error'
      },
      { status: 500 }
    );
  }
}

export async function GET() {
  return NextResponse.json({
    message: 'Promotion webhook endpoint is active',
    endpoints: {
      POST: '/api/webhooks/promotions'
    },
    supported_events: [
      'promotion.created',
      'promotion.updated',
      'promotion.deleted',
      'promotion.redeemed'
    ]
  });
}