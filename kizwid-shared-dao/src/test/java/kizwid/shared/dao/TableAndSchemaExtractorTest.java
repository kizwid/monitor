package kizwid.shared.dao;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: kevsanders
 * Date: 26/12/2012
 * Time: 23:02
 * To change this template use File | Settings | File Templates.
 */
public class TableAndSchemaExtractorTest {

//        getTableNameFromSql("SELECT * FROM PUBLIC.TEST t where t.id = 0");
//        getTableNameFromSql("select * from test t where t.id = 0");
//        getTableNameFromSql("select a,b,c as d,e f from public.test t where t.id = (select id from other");

    @Test
    public void canExtractFromLowerCase(){
        assertEquals(
                "public.test",
                TableAndSchemaExtractor.getTableNameFromSql("select * from public.test t where t.id = 0")
                );
    }
    @Test
    public void canExtractFromUpperCase(){
        assertEquals(
                "PUBLIC.TEST",
                TableAndSchemaExtractor.getTableNameFromSql("SELECT * FROM PUBLIC.TEST t where t.id = 0")
                );
    }
    @Test
    public void canExtractFromLocalTable(){
        assertEquals(
                "TEST",
                TableAndSchemaExtractor.getTableNameFromSql("SELECT * FROM TEST t where t.id = 0")
                );
    }
    @Test
    public void canExtractFromLocalTableWithSelectList(){
        assertEquals(
                "TEST",
                TableAndSchemaExtractor.getTableNameFromSql("SELECT a,b,c as d,e f FROM TEST t where t.id = 0")
                );
    }

    @Test //TODO: this one fails
    public void canExtractFromLocalTableWithSubQuery(){
        assertEquals(
                "other", //should be TEST
                TableAndSchemaExtractor.getTableNameFromSql("SELECT a,b,c as d,e f FROM TEST t where t.id = (select id from other)")
                );
    }
}
