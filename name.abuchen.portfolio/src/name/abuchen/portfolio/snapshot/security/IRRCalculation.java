package name.abuchen.portfolio.snapshot.security;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import name.abuchen.portfolio.math.IRR;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.CurrencyConverter;
import name.abuchen.portfolio.money.Values;

/* package */class IRRCalculation extends Calculation
{
    private List<LocalDate> dates = new ArrayList<>();
    private List<Double> values = new ArrayList<>();

    @Override
    public void visit(CurrencyConverter converter, CalculationLineItem.ValuationAtStart t)
    {
        dates.add(t.getDateTime().toLocalDate());
        values.add(-t.getValue().with(converter.at(t.getDateTime())).getAmount() / Values.Amount.divider());
    }

    @Override
    public void visit(CurrencyConverter converter, CalculationLineItem.ValuationAtEnd t)
    {
        dates.add(t.getDateTime().toLocalDate());
        values.add(t.getValue().with(converter.at(t.getDateTime())).getAmount() / Values.Amount.divider());
    }

    @Override
    public void visit(CurrencyConverter converter, CalculationLineItem.DividendPayment t)
    {
        dates.add(t.getDateTime().toLocalDate());

        long taxes = t.getTransaction().orElseThrow(IllegalArgumentException::new).getUnitSum(Unit.Type.TAX, converter)
                        .getAmount();
        long amount = t.getValue().with(converter.at(t.getDateTime())).getAmount();

        values.add((amount + taxes) / Values.Amount.divider());
    }

    @Override
    public void visit(CurrencyConverter converter, CalculationLineItem.TransactionItem item, AccountTransaction t)
    {
        switch (t.getType())
        {
            case TAXES:
            case TAX_REFUND:
                // ignore tax and tax refunds when calculating the irr for a
                // single security
                break;
            case FEES:
                dates.add(t.getDateTime().toLocalDate());
                values.add(-converter.convert(t.getDateTime(), t.getMonetaryAmount()).getAmount()
                                / Values.Amount.divider());
                break;
            case FEES_REFUND:
                dates.add(t.getDateTime().toLocalDate());
                values.add(converter.convert(t.getDateTime(), t.getMonetaryAmount()).getAmount()
                                / Values.Amount.divider());
                break;
            default:
        }
    }

    @Override
    public void visit(CurrencyConverter converter, CalculationLineItem.TransactionItem item, PortfolioTransaction t)
    {
        dates.add(t.getDateTime().toLocalDate());
        long taxes = t.getUnitSum(Unit.Type.TAX, converter).getAmount();
        long amount = t.getMonetaryAmount(converter).getAmount();
        switch (t.getType())
        {
            case BUY:
            case DELIVERY_INBOUND:
            case TRANSFER_IN:
                values.add((-amount + taxes) / Values.Amount.divider());
                break;
            case SELL:
            case DELIVERY_OUTBOUND:
            case TRANSFER_OUT:
                values.add((amount + taxes) / Values.Amount.divider());
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    public double getIRR()
    {
        // see #457: if the reporting period contains only tax refunds, dates
        // (and values) can be empty and no IRR can be calculated
        if (dates.isEmpty())
            return Double.NaN;

        return IRR.calculate(dates, values);
    }
}
