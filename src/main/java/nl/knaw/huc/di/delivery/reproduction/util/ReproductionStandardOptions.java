package nl.knaw.huc.di.delivery.reproduction.util;

import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionCustomNote;
import nl.knaw.huc.di.delivery.reproduction.entity.ReproductionStandardOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a list of reproduction standard options.
 */
public class ReproductionStandardOptions {
    private List<ReproductionStandardOption> options = new ArrayList<>();
    private List<ReproductionCustomNote> customNotes = new ArrayList<>();

    public ReproductionStandardOptions() {
    }

    public ReproductionStandardOptions(List<ReproductionStandardOption> options,
                                       List<ReproductionCustomNote> customNotes) {
        setOptions(options);
        setCustomNotes(customNotes);
    }

    public List<ReproductionStandardOption> getOptions() {
        return options;
    }

    public void setOptions(List<ReproductionStandardOption> options) {
        this.options = options;
    }

    public List<ReproductionCustomNote> getCustomNotes() {
        return customNotes;
    }

    public void setCustomNotes(List<ReproductionCustomNote> customNotes) {
        this.customNotes = customNotes;
    }
}
