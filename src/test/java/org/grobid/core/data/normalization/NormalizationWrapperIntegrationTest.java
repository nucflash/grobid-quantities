package org.grobid.core.data.normalization;

import org.grobid.core.data.Quantity;
import org.grobid.core.data.Unit;
import org.grobid.core.engines.UnitParser;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import tec.uom.se.unit.ProductUnit;
import tec.uom.se.unit.TransformedUnit;

import javax.measure.format.UnitFormat;
import javax.measure.spi.Bootstrap;
import javax.measure.spi.UnitFormatService;
import java.math.BigDecimal;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 14.02.16.
 */
@Ignore("Integration test")
public class NormalizationWrapperIntegrationTest {

    private NormalizationWrapper target;
    UnitParser mockUnitParser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        LibraryLoader.load();
    }

    @Before
    public void setUp() throws Exception {
        target = new NormalizationWrapper();
        mockUnitParser = createMock(UnitParser.class);
        //target.setUnitParser(mockUnitParser);
    }

    @Test
    public void testParse_baseUnit_noPow() throws Exception {
        String unitSymbol = "m";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);

        assertThat(normalized.getSymbol(), is("m"));
    }

    @Ignore("Not yet supported")
    @Test
    public void testParse_baseUnit_fullName_noPow() throws Exception {
        String unitSymbol = "meter";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);

        assertThat(normalized.getSymbol(), is("m"));
    }

    @Test
    public void testParse_composedUnit_noPow() throws Exception {
        String unitSymbol = "hm";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);

        assertThat(((TransformedUnit) normalized).getParentUnit().getSymbol(), is("m"));
    }

    @Test
    public void testParse_baseUnit_pow2() throws Exception {
        String unitSymbol = "m^2";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);
        assertThat(normalized.getProductUnits().size(), is(1));
        String decomposedUnit = "m";

        assertThat(normalized.getSymbol(), is(decomposedUnit));

    }

    @Test
    public void testParse_transformedUnit_pow2() throws Exception {
        String unitSymbol = "km^2";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);
        assertThat(normalized.getProductUnits().size(), is(1));
        String decomposedUnit = "km";
        assertThat(normalized, is(ProductUnit.class));
        assertThat(normalized.getProductUnits().size(), is(1));
    }

    @Test
    public void testParse_productUnit_pow2() throws Exception {
        String unitSymbol = "m/km^2";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);
        Map productUnits = normalized.getProductUnits();
        assertThat(normalized, is(ProductUnit.class));
        assertThat(productUnits.size(), is(2));
    }

    @Test
    public void testParse_productUnit_celsius() throws Exception {
        String unitSymbol = "m*°C";
        javax.measure.Unit normalized = target.parseUnit(unitSymbol);

        assertThat(normalized.getProductUnits().size(), is(2));

    }


    @Ignore("Obsolete")
    @Test(expected = NormalizationException.class)
    public void testParse_productUnit_unknown() throws Exception {
        String unitSymbol = "m*Jy";
        target.parseUnit(unitSymbol);
    }

    @Ignore("Obsolete")
    @Test(expected = NormalizationException.class)
    public void testNormalization_productUnit_unknown2() throws Exception {
        String unitSymbol = "μ*m";
        target.parseUnit(unitSymbol);
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithoutNormalization_meters() throws Exception {
        Quantity input = new Quantity();
        input.setValue("2");
        Unit raw = new Unit();
        raw.setRawName("m");
        input.setRawUnit(raw);

        Quantity.Normalized output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getUnit().getRawName(), is("m"));
        assertThat(output.getValue().doubleValue(), is(2.0));
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithNormalization_kmToMeters() throws Exception {
        Quantity input = new Quantity();
        input.setValue("2");
        Unit raw = new Unit();
        raw.setRawName("km");
        input.setRawUnit(raw);

        Quantity.Normalized output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getUnit().getRawName(), is("m"));
        assertThat(output.getValue().doubleValue(), is(2000.0));
    }

    @Test
    public void testNormalizeQuantity_simpleUnitWithNormalization_CelsiusToKelvin() throws Exception {
        Quantity input = new Quantity();
        input.setValue("10");
        Unit raw = new Unit();
        raw.setRawName("°C");
        input.setRawUnit(raw);

        Quantity.Normalized output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getUnit().getRawName(), is("K"));
        assertThat(output.getValue().doubleValue(), is(283.15));
    }

    @Test
    public void testNormalizeQuantity_kmHourToMetersSecond() throws Exception {
        Quantity input = new Quantity();
        input.setValue("2");
        Unit raw = new Unit();
        raw.setRawName("km/h");
        input.setRawUnit(raw);

        Quantity.Normalized output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getUnit().getRawName(), is("m/s"));
        assertThat(output.getValue().doubleValue(), is(0.5555555555555556));
    }

    @Test
    public void testNormalizeQuantity2_kmHourToMetersSecond() throws Exception {
        Quantity input = new Quantity();
        input.setValue("2000");
        Unit raw = new Unit();
        raw.setRawName("km*g/h");
        input.setRawUnit(raw);

        Quantity.Normalized output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getValue().doubleValue(), is(0.5555555555555556));
        assertThat(output.getUnit().getRawName(), is("m·kg/s"));
    }

    @Test
    public void testNormalizeQuantity3_kmHourToMetersSecond() throws Exception {
        Quantity input = new Quantity();
        input.setValue("2000");
        Unit raw = new Unit();
        raw.setRawName("km*kg/h");
        input.setRawUnit(raw);

        Quantity.Normalized output = target.normalizeQuantityToBaseUnits(input);
        assertThat(output.getValue().doubleValue(), is(555.5555555555555));
        assertThat(output.getUnit().getRawName(), is("m·kg/s"));
    }

    @Test
    public void testExtractProduct_productOfBaseUnits() throws Exception {
        String unitSymbol = "m/s";

        ProductUnit productUnit = (ProductUnit) target.parseUnit(unitSymbol);

        Map<String, Integer> output = target.extractProduct(productUnit);
        assertThat(output.size(), is(2));
        assertThat(output.get("s"), is(-1));
        assertThat(output.get("m"), is(1));
    }

    @Test
    public void testExtractProduct_productOfTransformedUnits() throws Exception {
        String unitSymbol = "km/h";

        ProductUnit productUnit = (ProductUnit) target.parseUnit(unitSymbol);

        Map<String, Integer> output = target.extractProduct(productUnit);
        assertThat(output.size(), is(2));
        assertThat(output.get("h"), is(-1));
        assertThat(output.get("km"), is(1));
    }

    @Test
    public void testExtractProduct_productOfSeveralTransformedUnits() throws Exception {
        String unitSymbol = "km/h*kg";

        ProductUnit productUnit = (ProductUnit) target.parseUnit(unitSymbol);

        Map<String, Integer> output = target.extractProduct(productUnit);
        assertThat(output.size(), is(3));
        assertThat(output.get("h"), is(-1));
        assertThat(output.get("km"), is(1));
        assertThat(output.get("kg"), is(-1));
    }

    @Test
    public void testCheckPrecision() throws Exception {
        UnitFormatService formatService = Bootstrap.getService(UnitFormatService.class);
        UnitFormat defaultFormatService = formatService.getUnitFormat();

        TransformedUnit unit = (TransformedUnit) defaultFormatService.parse("g");
        System.out.println("Conversion using double: " + unit.getSystemConverter().convert(0.39));
        System.out.println("Conversion using BigDecimal: " + (unit.getSystemConverter().convert(new BigDecimal("0.39"))));
        System.out.println("Conversion using BigDecimal output Double: " + new BigDecimal(unit.getSystemConverter().convert(new BigDecimal("0.39")).toString()).doubleValue());

        unit = (TransformedUnit) defaultFormatService.parse("%");
        System.out.println("Conversion using double: " + unit.getSystemConverter().convert(0.009));
        System.out.println("Conversion using BigDecimal: " + (unit.getSystemConverter().convert(new BigDecimal("0.009"))));
        System.out.println("Conversion using BigDecimal output Double: " + new BigDecimal(unit.getSystemConverter().convert(new BigDecimal("0.009")).toString()).doubleValue());
    }
}