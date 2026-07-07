//package kumaranai.dto;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class PreprocessRequest {
//
//    private String missingValueStrategy = "MEAN";
//    private String constantValue = "0";
//    private boolean removeDuplicates = true;
//    private boolean normalizeNumerical = false;
//    private boolean encodeCategorical = false;
//    private String normalizationType = "minmax";
//    private String encodingType = "label";
//
//    // ✅ KEY FIX — initialize to empty list, never null
//    private List<String> categoricalColumns = new ArrayList<>();
//    private List<String> numericalColumns   = new ArrayList<>();
//
//    private String inputFilePath;
//    private String outputFilePath;
//
//    // ── Getters ──────────────────────────────────────────────
//
//    public String getMissingValueStrategy()     { return missingValueStrategy; }
//    public String getConstantValue()            { return constantValue; }
//    public boolean isRemoveDuplicates()         { return removeDuplicates; }
//    public boolean isNormalizeNumerical()       { return normalizeNumerical; }
//    public boolean isEncodeCategorical()        { return encodeCategorical; }
//    public String getNormalizationType()        { return normalizationType; }
//    public String getEncodingType()             { return encodingType; }
//    public List<String> getCategoricalColumns() { return categoricalColumns; }
//    public List<String> getNumericalColumns()   { return numericalColumns; }
//    public String getInputFilePath()            { return inputFilePath; }
//    public String getOutputFilePath()           { return outputFilePath; }
//
//    // ── Setters ──────────────────────────────────────────────
//
//    public void setMissingValueStrategy(String v)  { this.missingValueStrategy = v; }
//    public void setConstantValue(String v)         { this.constantValue = v; }
//    public void setRemoveDuplicates(boolean v)     { this.removeDuplicates = v; }
//    public void setNormalizeNumerical(boolean v)   { this.normalizeNumerical = v; }
//    public void setEncodeCategorical(boolean v)    { this.encodeCategorical = v; }
//    public void setNormalizationType(String v)     { this.normalizationType = v; }
//    public void setEncodingType(String v)          { this.encodingType = v; }
//
//    // ✅ Null-safe setters — if Jackson sends null, fall back to empty list
//    public void setCategoricalColumns(List<String> v) {
//        this.categoricalColumns = (v != null) ? v : new ArrayList<>();
//    }
//    public void setNumericalColumns(List<String> v) {
//        this.numericalColumns = (v != null) ? v : new ArrayList<>();
//    }
//
//    public void setInputFilePath(String v)  { this.inputFilePath = v; }
//    public void setOutputFilePath(String v) { this.outputFilePath = v; }
//}