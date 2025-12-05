package org.socialhistoryservices.delivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "delivery")
public class DeliveryProperties {
    private String apiBase = "/solr/all/srw";
    private String apiDomain = "api.socialhistoryservices.org";
    private int apiPort = 443;
    private String apiProto = "https";
    private String timezone = "Europe/Amsterdam";
    private String dateFormat = "yyyy-MM-dd";
    private int externalInfoMinDaysCache = 30;
    private String holdingSeparator = "^";
    private String itemSeparator = ".";
    private String ldapManagerDn = "cn=admin,dc=socialhistoryservices,dc=org";
    private String ldapManagerPassword = "test";
    private String ldapUrl = "ldap://ds4.socialhistoryservices.org/dc=socialhistoryservices,dc=org";
    private String ldapUserSearchBase = "ou=users";
    private String ldapUserSearchFilter = "cn={0}";
    private boolean mailEnabled = true;
    private String mailReadingRoom = "blabla@iisg.nl";
    private String mailSystemAddressReadingRoom = "n0r3ply@iisg.nl";
    private String mailRepro = "blablablabla@iisg.nl";
    private String mailSystemAddressRepro = "n0r3ply@iisg.nl";
    private String mollieApiKey = "bla";
    private String mollieProfile = "bla";
    private int permissionMaxPageLen = 100;
    private int permissionPageLen = 20;
    private int permissionPageStepSize = 10;
    private String pidSeparator = ",";
    private int copyrightYear = 70;
    private int reproductionAdministrationCosts = 6;
    private int reproductionAdministrationCostsMinPages = 50;
    private int reproductionMaxDaysPayment = 21;
    private int reproductionMaxDaysReminder = 14;
    private int reproductionBtwPercentage = 21;
    private String requestAutoPrintStartTime = "9:00";
    private String requestLatestTime = "15:00";
    private int requestMaxPageLen = 100;
    private int requestPageLen = 20;
    private int requestPageStepSize = 10;
    private int reservationMaxDaysInAdvance = 31;
    private int reservationMaxItems = 3;
    private int reservationMaxChildren = 10;
    private boolean printEnabled = true;
    private String printerArchive = "delivery-archive";
    private String printerReadingRoom = "delivery-reading-room";
    private String sorAccessToken = "bla";
    private String sorAddress = "http://disseminate.objectrepository.org";
    private String timeFormat = "HH:mm:ss";
    private String urlSearch = "search-acc.socialhistory.org";
    private String urlSelf = "http://localhost:8080";
    private int recordPageLen = 20;
    private String profile = "white";
    private String gitClosestTagName = "";
    private String gitCommitId = "";
    private String gitBuildVersion = "";

    public DeliveryProperties() {

        final ClassLoader classLoader = getClass().getClassLoader();
        final InputStream inputStream = classLoader.getResourceAsStream("git.properties");
        if (inputStream != null) {
            final Properties properties = new Properties();
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            @SuppressWarnings("unchecked")
            final Enumeration<String> enums = (Enumeration<String>) properties.propertyNames();
            while (enums.hasMoreElements()) {
                final String key = enums.nextElement();
                final String value = properties.getProperty(key);

                switch (key) {
                    case "git.closest.tag.name":
                        this.gitClosestTagName = value;
                        break;
                    case "git.commit.id":
                        this.gitCommitId=value;
                    case "git.build.version":
                        this.gitBuildVersion = value;
                        break;
                    default:
                        // ignore
                        break;
                }
            }
        }
    }

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public void setMailEnabled(boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    public boolean isPrintEnabled() {
        return printEnabled;
    }

    public void setPrintEnabled(boolean printEnabled) {
        this.printEnabled = printEnabled;
    }

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

    public String getLdapManagerDn() {
        return ldapManagerDn;
    }

    public void setLdapManagerDn(String ldapManagerDn) {
        this.ldapManagerDn = ldapManagerDn;
    }

    public String getLdapManagerPassword() {
        return ldapManagerPassword;
    }

    public void setLdapManagerPassword(String ldapManagerPassword) {
        this.ldapManagerPassword = ldapManagerPassword;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getLdapUserSearchBase() {
        return ldapUserSearchBase;
    }

    public void setLdapUserSearchBase(String ldapUserSearchBase) {
        this.ldapUserSearchBase = ldapUserSearchBase;
    }

    public String getLdapUserSearchFilter() {
        return ldapUserSearchFilter;
    }

    public void setLdapUserSearchFilter(String ldapUserSearchFilter) {
        this.ldapUserSearchFilter = ldapUserSearchFilter;
    }

    public String getMailReadingRoom() {
        return mailReadingRoom;
    }

    public void setMailReadingRoom(String mailReadingRoom) {
        this.mailReadingRoom = mailReadingRoom;
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

    public String getMailSystemAddressReadingRoom() {
        return mailSystemAddressReadingRoom;
    }

    public void setMailSystemAddressReadingRoom(String mailSystemAddressReadingRoom) {
        this.mailSystemAddressReadingRoom = mailSystemAddressReadingRoom;
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

    public void setReproductionAdministrationCostsMinPages(int reproductionAdministrationCostsMinimumPages) {
        this.reproductionAdministrationCostsMinPages = reproductionAdministrationCostsMinimumPages;
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

    public String getGitClosestTagName() {
        return gitClosestTagName;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public String getGitBuildVersion() {
        return gitBuildVersion;
    }
}
