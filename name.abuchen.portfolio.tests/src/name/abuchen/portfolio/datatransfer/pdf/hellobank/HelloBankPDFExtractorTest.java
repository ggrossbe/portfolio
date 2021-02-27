package name.abuchen.portfolio.datatransfer.pdf.hellobank;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import name.abuchen.portfolio.datatransfer.Extractor;
import name.abuchen.portfolio.datatransfer.Extractor.BuySellEntryItem;
import name.abuchen.portfolio.datatransfer.Extractor.Item;
import name.abuchen.portfolio.datatransfer.Extractor.SecurityItem;
import name.abuchen.portfolio.datatransfer.Extractor.TransactionItem;
import name.abuchen.portfolio.datatransfer.actions.AssertImportActions;
import name.abuchen.portfolio.datatransfer.pdf.HelloBankPDFExtractor;
import name.abuchen.portfolio.datatransfer.pdf.PDFInputFile;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.model.Security;
import name.abuchen.portfolio.model.Transaction.Unit;
import name.abuchen.portfolio.money.CurrencyUnit;
import name.abuchen.portfolio.money.Money;
import name.abuchen.portfolio.money.Values;

@SuppressWarnings("nls")
public class HelloBankPDFExtractorTest
{
    @Test
    public void testErtrag01() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Ertrag01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("NO0003054108"));
        assertThat(security.getName(), is("M a r i n e  H a r v est ASA"));
        assertThat(security.getCurrencyCode(), is("NOK"));

        // check transaction
        item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(AccountTransaction.class));
        AccountTransaction transaction = (AccountTransaction) item.get().getSubject();
        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(transaction.getSecurity(), is(security));
        assertThat(transaction.getDateTime(), is(LocalDateTime.parse("2017-09-06T00:00")));
        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(48.71))));
        assertThat(transaction.getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(0.95 + 0.19 + (176.01 / 9.308)))));
        assertThat(transaction.getShares(), is(Values.Share.factorize(200)));

        Unit grossValueUnit = transaction.getUnit(Unit.Type.GROSS_VALUE).get();
        assertThat(grossValueUnit.getAmount(), is(Money.of("EUR", Values.Amount.factorize(640 / 9.308))));
        assertThat(grossValueUnit.getForex(), is(Money.of("NOK", Values.Amount.factorize(640))));
        assertThat(grossValueUnit.getExchangeRate(),
                        is(BigDecimal.ONE.divide(BigDecimal.valueOf(9.308), 10, RoundingMode.HALF_UP)));

        assertThat(grossValueUnit.getAmount().getAmount() - transaction.getUnitSum(Unit.Type.TAX).getAmount(),
                        is(transaction.getMonetaryAmount().getAmount()));
    }

    @Test
    public void testErtrag01WithExistingSecurity() throws IOException
    {
        Security security = new Security("Marine Harvest ASA", CurrencyUnit.EUR);
        security.setIsin("NO0003054108");

        Client client = new Client();
        client.addSecurity(security);

        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Ertrag01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(AccountTransaction.class));
        AccountTransaction transaction = (AccountTransaction) item.get().getSubject();
        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(transaction.getSecurity(), is(security));
        assertThat(transaction.getDateTime(), is(LocalDateTime.parse("2017-09-06T00:00")));
        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(48.71))));

        assertThat(transaction.getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(0.95 + 0.19 + (176.01 / 9.308)))));
        assertThat(transaction.getShares(), is(Values.Share.factorize(200)));
    }

    @Test
    public void testErtrag02() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Ertrag02.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("US56035L1044"));
        assertThat(security.getName(), is("M a i n  S t r e e t Capital Corp."));
        assertThat(security.getCurrencyCode(), is("USD"));

        // check transaction
        item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(AccountTransaction.class));
        AccountTransaction transaction = (AccountTransaction) item.get().getSubject();
        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(transaction.getSecurity(), is(security));
        assertThat(transaction.getDateTime(), is(LocalDateTime.parse("2017-05-15T00:00")));
        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(12.34))));
        assertThat(transaction.getUnitSum(Unit.Type.TAX), is(
                        Money.of(CurrencyUnit.EUR, Values.Amount.factorize(0.95 + 0.19 + ((3.05 + 2.55) / 1.0942)))));
        assertThat(transaction.getShares(), is(Values.Share.factorize(110)));

        Unit grossValueUnit = transaction.getUnit(Unit.Type.GROSS_VALUE).get();
        assertThat(grossValueUnit.getAmount(), is(Money.of("EUR", Values.Amount.factorize(20.35 / 1.0942))));
        assertThat(grossValueUnit.getForex(), is(Money.of("USD", Values.Amount.factorize(20.35))));
        assertThat(grossValueUnit.getExchangeRate(),
                        is(BigDecimal.ONE.divide(BigDecimal.valueOf(1.0942), 10, RoundingMode.HALF_UP)));

        assertThat(grossValueUnit.getAmount().getAmount() - transaction.getUnitSum(Unit.Type.TAX).getAmount(),
                        is(transaction.getMonetaryAmount().getAmount()));
    }

    @Test
    public void testErtrag03() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Ertrag03.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("NL0012325773"));
        assertThat(security.getName(), is("R o y a l  D u t c h Shell PLC"));
        assertThat(security.getCurrencyCode(), is("EUR"));

        // check transaction
        item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(AccountTransaction.class));
        AccountTransaction transaction = (AccountTransaction) item.get().getSubject();
        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(transaction.getSecurity(), is(security));
        assertThat(transaction.getDateTime(), is(LocalDateTime.parse("2017-06-26T00:00")));
        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(41.43))));
        assertThat(transaction.getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(0.95 + 0.19 + 8.81 + 7.34))));
        assertThat(transaction.getShares(), is(Values.Share.factorize(140)));

        assertThat(Values.Amount.factorize(58.72) - transaction.getUnitSum(Unit.Type.TAX).getAmount(),
                        is(transaction.getMonetaryAmount().getAmount()));
    }

    @Test
    public void testErtrag04() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Ertrag04.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("US3682872078"));
        assertThat(security.getName(), is("G a z p r o m  P J S C"));
        assertThat(security.getCurrencyCode(), is("USD"));

        // check transaction
        item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(AccountTransaction.class));
        AccountTransaction transaction = (AccountTransaction) item.get().getSubject();
        assertThat(transaction.getType(), is(AccountTransaction.Type.DIVIDENDS));
        assertThat(transaction.getSecurity(), is(security));
        assertThat(transaction.getDateTime(), is(LocalDateTime.parse("2017-08-21T00:00")));
        assertThat(transaction.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(116.91))));
        assertThat(transaction.getUnitSum(Unit.Type.TAX), is(Money.of(CurrencyUnit.EUR,
                        Values.Amount.factorize(0.19 + 0.95 + ((32.14 + 26.79 + 16) / 1.1805)))));
        assertThat(transaction.getShares(), is(Values.Share.factorize(800)));

        Unit grossValueUnit = transaction.getUnit(Unit.Type.GROSS_VALUE).get();
        assertThat(grossValueUnit.getAmount(), is(Money.of("EUR", Values.Amount.factorize(214.28 / 1.1805))));
        assertThat(grossValueUnit.getForex(), is(Money.of("USD", Values.Amount.factorize(214.28))));
        assertThat(grossValueUnit.getExchangeRate(),
                        is(BigDecimal.ONE.divide(BigDecimal.valueOf(1.1805), 10, RoundingMode.HALF_UP)));

        assertThat(grossValueUnit.getAmount().getAmount() - transaction.getUnitSum(Unit.Type.TAX).getAmount(),
                        is(transaction.getMonetaryAmount().getAmount()));
    }

    @Test
    public void testKauf01() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Kauf01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("NO0003054108"));
        assertThat(security.getName(), is("M a r i n e  H a r v est ASA"));
        assertThat(security.getCurrencyCode(), is("NOK"));

        // check transaction
        item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));

        BuySellEntry entry = (BuySellEntry) item.get().getSubject();
        PortfolioTransaction tx = entry.getPortfolioTransaction();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(1118.8))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2017-06-30T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(74)));
        assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(25.20 + 1.46))));

        Unit grossValueUnit = tx.getUnit(Unit.Type.GROSS_VALUE).get();
        assertThat(grossValueUnit.getAmount(), is(Money.of("EUR", Values.Amount.factorize(1092.14))));
        assertThat(grossValueUnit.getForex(), is(Money.of("NOK", Values.Amount.factorize(10360))));
        assertThat(grossValueUnit.getExchangeRate(),
                        is(BigDecimal.ONE.divide(BigDecimal.valueOf(9.486), 10, RoundingMode.HALF_UP)));
    }

    @Test
    public void testKauf01WithExistingSecurity() throws IOException
    {
        Security security = new Security("Marine Harvest ASA", CurrencyUnit.EUR);
        security.setIsin("NO0003054108");

        Client client = new Client();
        client.addSecurity(security);

        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Kauf01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));

        BuySellEntry entry = (BuySellEntry) item.get().getSubject();
        PortfolioTransaction tx = entry.getPortfolioTransaction();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(1118.8))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2017-06-30T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(74)));
        assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(25.20 + 1.46))));
    }

    @Test
    public void testKauf02() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Kauf02.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("GB00B03MLX29"));
        assertThat(security.getName(), is("R o y a l  D u t c h Shell"));
        assertThat(security.getCurrencyCode(), is("GBP"));

        // check transaction
        item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));

        BuySellEntry entry = (BuySellEntry) item.get().getSubject();
        PortfolioTransaction tx = entry.getPortfolioTransaction();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.BUY));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(1314.03))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2017-04-27T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(55)));
        assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(6.53 + 1.55))));

        Unit grossValueUnit = tx.getUnit(Unit.Type.GROSS_VALUE).get();
        assertThat(grossValueUnit.getAmount(), is(Money.of("EUR", Values.Amount.factorize(1305.95))));
        assertThat(grossValueUnit.getForex(), is(Money.of("GBP", Values.Amount.factorize(1100))));
        assertThat(grossValueUnit.getExchangeRate(),
                        is(BigDecimal.ONE.divide(BigDecimal.valueOf(0.8423), 10, RoundingMode.HALF_UP)));
    }

    @Test
    public void testVerkauf01() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Verkauf01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("AU000000SHV6"));
        assertThat(security.getName(), is("S E L E C T  H A R V EST LTD."));
        assertThat(security.getCurrencyCode(), is("AUD"));

        // check transaction
        item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));

        BuySellEntry entry = (BuySellEntry) item.get().getSubject();
        PortfolioTransaction tx = entry.getPortfolioTransaction();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(3096.85))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2017-10-12T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(1000)));
        assertThat(tx.getUnitSum(Unit.Type.FEE),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(14.99 + 6.42 + 7.95))));
        assertThat(tx.getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(254.1 / 1.5181))));

        Unit grossValueUnit = tx.getUnit(Unit.Type.GROSS_VALUE).get();
        assertThat(grossValueUnit.getAmount(), is(Money.of("EUR", Values.Amount.factorize(5000 / 1.5181))));
        assertThat(grossValueUnit.getForex(), is(Money.of("AUD", Values.Amount.factorize(5000))));
        assertThat(grossValueUnit.getExchangeRate(),
                        is(BigDecimal.ONE.divide(BigDecimal.valueOf(1.5181), 10, RoundingMode.HALF_UP)));

        assertThat(grossValueUnit.getAmount().getAmount() - tx.getUnitSum(Unit.Type.TAX).getAmount()
                        - tx.getUnitSum(Unit.Type.FEE).getAmount(), is(tx.getMonetaryAmount().getAmount()));

    }

    @Test
    public void testVerkauf01WithExistingSecurity() throws IOException
    {
        Security security = new Security("SELECT HARVEST LTD.", CurrencyUnit.EUR);
        security.setIsin("AU000000SHV6");

        Client client = new Client();
        client.addSecurity(security);

        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(client);

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Verkauf01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(1));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check transaction
        Optional<Item> item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));

        BuySellEntry entry = (BuySellEntry) item.get().getSubject();
        PortfolioTransaction tx = entry.getPortfolioTransaction();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(3096.85))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2017-10-12T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(1000)));
        assertThat(tx.getUnitSum(Unit.Type.FEE),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(14.99 + 6.42 + 7.95))));
        assertThat(tx.getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(254.1 / 1.5181))));
    }

    @Test
    public void testInboundDelivery01() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "InboundDelivery01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("DK0060534915"));
        assertThat(security.getName(), is("N o v o - N o r d i s k AS"));
        assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));

        // check transaction
        item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(PortfolioTransaction.class));

        PortfolioTransaction tx = (PortfolioTransaction) item.get().getSubject();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.DELIVERY_INBOUND));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(3225.37))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2017-03-29T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(80)));
    }

    @Test
    public void testInboundDelivery02() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "InboundDelivery02.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("US56035L1044"));
        assertThat(security.getName(), is("M a i n  S t r e e t Capital Corp."));
        assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));

        // check transaction
        item = results.stream().filter(i -> i instanceof TransactionItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(PortfolioTransaction.class));

        PortfolioTransaction tx = (PortfolioTransaction) item.get().getSubject();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.DELIVERY_INBOUND));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(3021.70))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2017-03-31T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(110)));
    }

    @Test
    public void testKontoauszug01() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Kontoauszug01.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(14));
        new AssertImportActions().check(results, CurrencyUnit.EUR);

        Item item;
        Iterator<Extractor.Item> iter;
        // check Securities
        /*
        iter = results.stream().filter(i -> i instanceof SecurityItem).iterator();
        while (iter.hasNext())
        {
          item = iter.next();
          Security security = item.getSecurity();
          System.out.println(security.getIsin());
        }
        */
        iter = results.stream().filter(i -> i instanceof SecurityItem).iterator();
        if (iter.hasNext())
        {
            item = iter.next();
            Security security = item.getSecurity();

            // assert security
            assertThat(security.getIsin(), is("DE000LS9HP25"));
            assertThat(security.getName(), is("L a n g  &  S c h w arz AG"));
            assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            Security security = item.getSecurity();

            // assert security
            assertThat(security.getIsin(), is("DE000LS9BYR7"));
            assertThat(security.getName(), is("L a n g  &  S c h w arz AG"));
            assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            Security security = item.getSecurity();

            // assert security
            assertThat(security.getIsin(), is("DE000LS9KKY3"));
            assertThat(security.getName(), is("L a n g  &  S c h w arz AG"));
            assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            Security security = item.getSecurity();

            // assert security
            assertThat(security.getIsin(), is("AU000000KP25"));
            assertThat(security.getName(), is("K o r e  P o t a s h PLC"));
            assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            Security security = item.getSecurity();

            // assert security
            assertThat(security.getIsin(), is("AU000000LSA2"));
            assertThat(security.getName(), is("L a c h l a n  S t a r Ltd."));
            assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            Security security = item.getSecurity();

            // assert security
            assertThat(security.getIsin(), is("AU0000048001"));
            assertThat(security.getName(), is("A L  L e g a l  G r oup Ltd."));
            assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            Security security = item.getSecurity();

            // assert security
            assertThat(security.getIsin(), is("CA95942C1041"));
            assertThat(security.getName(), is("W e s t e r n  R e s ources Corp."));
            assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));
        }

        // get transactions
        /*
        iter = results.stream().filter(i -> i instanceof BuySellEntryItem).iterator();
        while (iter.hasNext())
        {
          item = iter.next();
          BuySellEntry entry = (BuySellEntry) item.getSubject();
          System.out.println(entry.getPortfolioTransaction().getSecurity().getIsin());
        }
        */
        iter = results.stream().filter(i -> i instanceof BuySellEntryItem).iterator();
        if (iter.hasNext())
        {
            item = iter.next();
            BuySellEntry entry = (BuySellEntry) item.getSubject();

            // assert transaction
            PortfolioTransaction tx = entry.getPortfolioTransaction();
            assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));
            assertThat(tx.getType(), is(PortfolioTransaction.Type.BUY));
            assertThat(tx.getAmount(), is(Values.Amount.factorize(4525.94)));
            assertThat(tx.getDateTime(), is(LocalDateTime.parse("2021-01-05T00:00")));
            assertThat(tx.getShares(), is(Values.Share.factorize(11)));
            // tx.getUnits().filter(u -> u.getType() == Unit.Type.FEE).forEach(System.out::println);

            assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(7.90+5.95))));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            BuySellEntry entry = (BuySellEntry) item.getSubject();

            // assert transaction
            PortfolioTransaction tx = entry.getPortfolioTransaction();
            assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));
            assertThat(tx.getType(), is(PortfolioTransaction.Type.BUY));
            assertThat(tx.getAmount(), is(Values.Amount.factorize(2653.37)));
            assertThat(tx.getDateTime(), is(LocalDateTime.parse("2021-01-08T00:00")));
            assertThat(tx.getShares(), is(Values.Share.factorize(8)));
            assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(4.62+5.95))));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            BuySellEntry entry = (BuySellEntry) item.getSubject();

            // assert transaction
            PortfolioTransaction tx = entry.getPortfolioTransaction();
            assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.BUY));
            assertThat(tx.getType(), is(PortfolioTransaction.Type.BUY));
            assertThat(tx.getAmount(), is(Values.Amount.factorize(2830.99)));
            assertThat(tx.getDateTime(), is(LocalDateTime.parse("2021-01-08T00:00")));
            assertThat(tx.getShares(), is(Values.Share.factorize(5)));
            assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(4.94+5.95))));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            BuySellEntry entry = (BuySellEntry) item.getSubject();

            // assert transaction
            PortfolioTransaction tx = entry.getPortfolioTransaction();
            assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));
            assertThat(tx.getType(), is(PortfolioTransaction.Type.SELL));
            assertThat(tx.getAmount(), is(Values.Amount.factorize(0.00)));
            assertThat(tx.getDateTime(), is(LocalDateTime.parse("2021-01-04T00:00")));
            assertThat(tx.getShares(), is(Values.Share.factorize(550)));
            assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(4.13))));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            BuySellEntry entry = (BuySellEntry) item.getSubject();

            // assert transaction
            PortfolioTransaction tx = entry.getPortfolioTransaction();
            assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));
            assertThat(tx.getType(), is(PortfolioTransaction.Type.SELL));
            assertThat(tx.getAmount(), is(Values.Amount.factorize(0.00)));
            assertThat(tx.getDateTime(), is(LocalDateTime.parse("2021-01-06T00:00")));
            assertThat(tx.getShares(), is(Values.Share.factorize(666)));
            assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(6.66))));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            BuySellEntry entry = (BuySellEntry) item.getSubject();

            // assert transaction
            PortfolioTransaction tx = entry.getPortfolioTransaction();
            assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));
            assertThat(tx.getType(), is(PortfolioTransaction.Type.SELL));
            assertThat(tx.getAmount(), is(Values.Amount.factorize(0.00)));
            assertThat(tx.getDateTime(), is(LocalDateTime.parse("2021-01-05T00:00")));
            assertThat(tx.getShares(), is(Values.Share.factorize(13)));
            assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(3.69))));
        }

        if (iter.hasNext())
        {
            item = iter.next();
            BuySellEntry entry = (BuySellEntry) item.getSubject();

            // assert transaction
            PortfolioTransaction tx = entry.getPortfolioTransaction();
            assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));
            assertThat(tx.getType(), is(PortfolioTransaction.Type.SELL));
            assertThat(tx.getAmount(), is(Values.Amount.factorize(36.45)));
            assertThat(tx.getDateTime(), is(LocalDateTime.parse("2021-01-08T00:00")));
            assertThat(tx.getShares(), is(Values.Share.factorize(440)));
            assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(5.23+0.09+7.95))));
        }
    }

    @Test
    public void testKontoauszug02() throws IOException
    {
        HelloBankPDFExtractor extractor = new HelloBankPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        List<Item> results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Kontoauszug02.txt"), errors);

        assertThat(errors, empty());
        assertThat(results.size(), is(2));

        /*
        System.out.println("*** Kontoauszug02: Item 0 type is " + results.get(0).getTypeInformation());
        System.out.println("*** Kontoauszug02: Item 0 is " + results.get(0));
        System.out.println("*** Kontoauszug02: Item 1 type is " + results.get(1).getTypeInformation());
        System.out.println("*** Kontoauszug02: Item 1 is " + results.get(1));
        */

        new AssertImportActions().check(results, CurrencyUnit.EUR);

        // check security
        Optional<Item> item = results.stream().filter(i -> i instanceof SecurityItem).findFirst();
        assertThat(item.isPresent(), is(true));
        Security security = ((SecurityItem) item.get()).getSecurity();
        assertThat(security.getIsin(), is("AU000000SAR9"));
        assertThat(security.getName(), is("S a r a c e n  M i n eral Holdings Ltd."));
        assertThat(security.getCurrencyCode(), is(CurrencyUnit.EUR));

        // check transaction
        item = results.stream().filter(i -> i instanceof BuySellEntryItem).findFirst();
        assertThat(item.isPresent(), is(true));
        assertThat(item.get().getSubject(), instanceOf(BuySellEntry.class));

        BuySellEntry entry = (BuySellEntry) item.get().getSubject();
        PortfolioTransaction tx = entry.getPortfolioTransaction();

        assertThat(tx.getType(), is(PortfolioTransaction.Type.SELL));
        assertThat(entry.getAccountTransaction().getType(), is(AccountTransaction.Type.SELL));

        assertThat(tx.getMonetaryAmount(), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(7808.19))));
        assertThat(tx.getDateTime(), is(LocalDateTime.parse("2020-08-12T00:00")));
        assertThat(tx.getShares(), is(Values.Share.factorize(3200)));
        assertThat(tx.getUnitSum(Unit.Type.FEE), is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(17.98 + 4.95))));
        assertThat(tx.getUnitSum(Unit.Type.TAX),
                        is(Money.of(CurrencyUnit.EUR, Values.Amount.factorize(2440.88))));
    }
}
