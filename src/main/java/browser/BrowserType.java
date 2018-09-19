package browser;

public enum BrowserType {
    Chrome("chromedriver"),
    Firefox("geckodriver");

    private String value;

    BrowserType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static BrowserType getEnum(String value) {
        for(BrowserType v : values())
            if(v.getValue().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }
}