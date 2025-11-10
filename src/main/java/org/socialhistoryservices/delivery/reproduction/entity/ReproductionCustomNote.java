package org.socialhistoryservices.delivery.reproduction.entity;

import org.socialhistoryservices.delivery.record.entity.ExternalRecordInfo;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * ReproductionCustomNote object representing a note for a custom reproduction.
 */
@Entity
@Table(name = "reproduction_custom_notes")
@Configurable
public class ReproductionCustomNote {
    /**
     * The ReproductionCustomNote's id.
     */
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    /**
     * Get the ReproductionCustomNote's id.
     *
     * @return the ReproductionCustomNote's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Set the ReproductionCustomNote's id.
     *
     * @param id the ReproductionCustomNote's id.
     */
    public void setId(int id) {
        this.id = id;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, unique = true)
    private ExternalRecordInfo.MaterialType materialType;

    /**
     * Get the material type.
     *
     * @return The material type.
     */
    public ExternalRecordInfo.MaterialType getMaterialType() {
        return materialType;
    }

    /**
     * Set the material type.
     *
     * @param type The material type.
     */
    public void setMaterialType(ExternalRecordInfo.MaterialType type) {
        materialType = type;
    }

    @Size(max = 511)
    @Column(name = "note_nl")
    private String noteNL;

    @Size(max = 511)
    @Column(name = "note_en")
    private String noteEN;

    /**
     * Get the note.
     *
     * @return the note.
     */
    public String getNote() {
        if (LocaleContextHolder.getLocale().getLanguage().equals("nl"))
            return noteNL;
        return noteEN;
    }

    /**
     * Get the note (Dutch).
     *
     * @return the note in Dutch.
     */
    public String getNoteNL() {
        return noteNL;
    }

    /**
     * Get the note (English).
     *
     * @return the note in English.
     */
    public String getNoteEN() {
        return noteEN;
    }

    /**
     * Set the note (Dutch).
     *
     * @param optionNote the note in Dutch.
     */
    public void setNoteNL(String optionNote) {
        this.noteNL = optionNote;
    }

    /**
     * Set the note (English).
     *
     * @param optionNote the note in English.
     */
    public void setNoteEN(String optionNote) {
        this.noteEN = optionNote;
    }

    /**
     * Merge the other custom note's fields into this custom note.
     *
     * @param other The other reproduction custom note.
     */
    public void mergeWith(ReproductionCustomNote other) {
        setNoteNL(other.getNoteNL());
        setNoteEN(other.getNoteEN());
    }
}
