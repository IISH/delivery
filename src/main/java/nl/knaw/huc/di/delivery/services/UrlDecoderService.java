package nl.knaw.huc.di.delivery.services;

public interface UrlDecoderService {
    /**
     * Split a set of pids given in a url to an array of pids.
     *
     * @param pids The combined pids (URL encoded).
     * @return Separate pids.
     */
    String[] getPidsFromURL(String pids);
}
