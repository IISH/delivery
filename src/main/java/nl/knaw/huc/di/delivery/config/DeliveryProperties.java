package nl.knaw.huc.di.delivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "delivery")
public class DeliveryProperties {
    private String apiBase ;
    private String apiDomain ;
    private int apiPort ;
    private String apiProto ;
    private String timezone ;
    private String dateFormat ;
    private int externalInfoMinDaysCache ;
    private String holdingSeparator ;
    private String itemSeparator ;
    private boolean doNotSentMail;
    private String loginUrl;
    private String mailReadingRoom ;
    private String mailSystemAddressReadingRoom ;
    private String mailRepro ;
    private String mailSystemAddressRepro ;
    private String mollieApiKey ;
    private String mollieProfile ;
    private int permissionMaxPageLen ;
    private int permissionPageLen;
    private int permissionPageStepSize;
    private String pidSeparator ;
    private int copyrightYear ;
    private int reproductionAdministrationCosts ;
    private int reproductionAdministrationCostsMinPages ;
    private int reproductionMaxDaysPayment ;
    private int reproductionMaxDaysReminder ;
    private int reproductionBtwPercentage ;
    private String requestAutoPrintStartTime ;
    private String requestLatestTime ;
    private int requestMaxPageLen ;
    private int requestPageLen ;
    private int requestPageStepSize ;
    private int reservationMaxDaysInAdvance ;
    private int reservationMaxItems;
    private int reservationMaxChildren ;
    private boolean printEnabled ;
    private String printerArchive ;
    private String printerReadingRoom ;
    private String sorAccessToken ;
    private String sorAddress ;
    private String timeFormat;
    private String urlSearch ;
    private String urlSelf ;
    private int recordPageLen ;
    private String profile ;
    private String namingAuthorityPrefix ;


    public String getApiBase() {
        return apiBase;
    }

    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    public String getApiDomain() {
        return apiDomain;
    }

    public void setApiDomain(String apiDomain) {
        this.apiDomain = apiDomain;
    }

    public int getApiPort() {
        return apiPort;
    }

    public void setApiPort(int apiPort) {
        this.apiPort = apiPort;
    }

    public String getApiProto() {
        return apiProto;
    }

    public void setApiProto(String apiProto) {
        this.apiProto = apiProto;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public int getExternalInfoMinDaysCache() {
        return externalInfoMinDaysCache;
    }

    public void setExternalInfoMinDaysCache(int externalInfoMinDaysCache) {
        this.externalInfoMinDaysCache = externalInfoMinDaysCache;
    }

    public String getHoldingSeparator() {
        return holdingSeparator;
    }

    public void setHoldingSeparator(String holdingSeparator) {
        this.holdingSeparator = holdingSeparator;
    }

    public String getItemSeparator() {
        return itemSeparator;
    }

    public void setItemSeparator(String itemSeparator) {
        this.itemSeparator = itemSeparator;
    }

    public boolean isDoNotSentMail() {
        return doNotSentMail;
    }

    public void setDoNotSentMail(boolean doNotSentMail) {
        this.doNotSentMail = doNotSentMail;
    }

    public String getMailReadingRoom() {
        return mailReadingRoom;
    }

    public void setMailReadingRoom(String mailReadingRoom) {
        this.mailReadingRoom = mailReadingRoom;
    }

    public String getMailSystemAddressReadingRoom() {
        return mailSystemAddressReadingRoom;
    }

    public void setMailSystemAddressReadingRoom(String mailSystemAddressReadingRoom) {
        this.mailSystemAddressReadingRoom = mailSystemAddressReadingRoom;
    }

    public String getMailRepro() {
        return mailRepro;
    }

    public void setMailRepro(String mailRepro) {
        this.mailRepro = mailRepro;
    }

    public String getMailSystemAddressRepro() {
        return mailSystemAddressRepro;
    }

    public void setMailSystemAddressRepro(String mailSystemAddressRepro) {
        this.mailSystemAddressRepro = mailSystemAddressRepro;
    }

    public String getMollieApiKey() {
        return mollieApiKey;
    }

    public void setMollieApiKey(String mollieApiKey) {
        this.mollieApiKey = mollieApiKey;
    }

    public String getMollieProfile() {
        return mollieProfile;
    }

    public void setMollieProfile(String mollieProfile) {
        this.mollieProfile = mollieProfile;
    }

    public int getPermissionMaxPageLen() {
        return permissionMaxPageLen;
    }

    public void setPermissionMaxPageLen(int permissionMaxPageLen) {
        this.permissionMaxPageLen = permissionMaxPageLen;
    }

    public int getPermissionPageLen() {
        return permissionPageLen;
    }

    public void setPermissionPageLen(int permissionPageLen) {
        this.permissionPageLen = permissionPageLen;
    }

    public int getPermissionPageStepSize() {
        return permissionPageStepSize;
    }

    public void setPermissionPageStepSize(int permissionPageStepSize) {
        this.permissionPageStepSize = permissionPageStepSize;
    }

    public String getPidSeparator() {
        return pidSeparator;
    }

    public void setPidSeparator(String pidSeparator) {
        this.pidSeparator = pidSeparator;
    }

    public int getCopyrightYear() {
        return copyrightYear;
    }

    public void setCopyrightYear(int copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    public int getReproductionAdministrationCosts() {
        return reproductionAdministrationCosts;
    }

    public void setReproductionAdministrationCosts(int reproductionAdministrationCosts) {
        this.reproductionAdministrationCosts = reproductionAdministrationCosts;
    }

    public int getReproductionAdministrationCostsMinPages() {
        return reproductionAdministrationCostsMinPages;
    }

    public void setReproductionAdministrationCostsMinPages(int reproductionAdministrationCostsMinPages) {
        this.reproductionAdministrationCostsMinPages = reproductionAdministrationCostsMinPages;
    }

    public int getReproductionMaxDaysPayment() {
        return reproductionMaxDaysPayment;
    }

    public void setReproductionMaxDaysPayment(int reproductionMaxDaysPayment) {
        this.reproductionMaxDaysPayment = reproductionMaxDaysPayment;
    }

    public int getReproductionMaxDaysReminder() {
        return reproductionMaxDaysReminder;
    }

    public void setReproductionMaxDaysReminder(int reproductionMaxDaysReminder) {
        this.reproductionMaxDaysReminder = reproductionMaxDaysReminder;
    }

    public int getReproductionBtwPercentage() {
        return reproductionBtwPercentage;
    }

    public void setReproductionBtwPercentage(int reproductionBtwPercentage) {
        this.reproductionBtwPercentage = reproductionBtwPercentage;
    }

    public String getRequestAutoPrintStartTime() {
        return requestAutoPrintStartTime;
    }

    public void setRequestAutoPrintStartTime(String requestAutoPrintStartTime) {
        this.requestAutoPrintStartTime = requestAutoPrintStartTime;
    }

    public String getRequestLatestTime() {
        return requestLatestTime;
    }

    public void setRequestLatestTime(String requestLatestTime) {
        this.requestLatestTime = requestLatestTime;
    }

    public int getRequestMaxPageLen() {
        return requestMaxPageLen;
    }

    public void setRequestMaxPageLen(int requestMaxPageLen) {
        this.requestMaxPageLen = requestMaxPageLen;
    }

    public int getRequestPageLen() {
        return requestPageLen;
    }

    public void setRequestPageLen(int requestPageLen) {
        this.requestPageLen = requestPageLen;
    }

    public int getRequestPageStepSize() {
        return requestPageStepSize;
    }

    public void setRequestPageStepSize(int requestPageStepSize) {
        this.requestPageStepSize = requestPageStepSize;
    }

    public int getReservationMaxDaysInAdvance() {
        return reservationMaxDaysInAdvance;
    }

    public void setReservationMaxDaysInAdvance(int reservationMaxDaysInAdvance) {
        this.reservationMaxDaysInAdvance = reservationMaxDaysInAdvance;
    }

    public int getReservationMaxItems() {
        return reservationMaxItems;
    }

    public void setReservationMaxItems(int reservationMaxItems) {
        this.reservationMaxItems = reservationMaxItems;
    }

    public int getReservationMaxChildren() {
        return reservationMaxChildren;
    }

    public void setReservationMaxChildren(int reservationMaxChildren) {
        this.reservationMaxChildren = reservationMaxChildren;
    }

    public boolean isPrintEnabled() {
        return printEnabled;
    }

    public void setPrintEnabled(boolean printEnabled) {
        this.printEnabled = printEnabled;
    }

    public String getPrinterArchive() {
        return printerArchive;
    }

    public void setPrinterArchive(String printerArchive) {
        this.printerArchive = printerArchive;
    }

    public String getPrinterReadingRoom() {
        return printerReadingRoom;
    }

    public void setPrinterReadingRoom(String printerReadingRoom) {
        this.printerReadingRoom = printerReadingRoom;
    }

    public String getSorAccessToken() {
        return sorAccessToken;
    }

    public void setSorAccessToken(String sorAccessToken) {
        this.sorAccessToken = sorAccessToken;
    }

    public String getSorAddress() {
        return sorAddress;
    }

    public void setSorAddress(String sorAddress) {
        this.sorAddress = sorAddress;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getUrlSearch() {
        return urlSearch;
    }

    public void setUrlSearch(String urlSearch) {
        this.urlSearch = urlSearch;
    }

    public String getUrlSelf() {
        return urlSelf;
    }

    public void setUrlSelf(String urlSelf) {
        this.urlSelf = urlSelf;
    }

    public int getRecordPageLen() {
        return recordPageLen;
    }

    public void setRecordPageLen(int recordPageLen) {
        this.recordPageLen = recordPageLen;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getNamingAuthorityPrefix() {
        return namingAuthorityPrefix;
    }

    public void setNamingAuthorityPrefix(String namingAuthorityPrefix) {
        this.namingAuthorityPrefix = namingAuthorityPrefix;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
}
