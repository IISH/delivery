package org.socialhistoryservices.delivery.record.entity;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents external info for a record (title from evergreen,
 * author etc.). This information is cached, cannot be relied upon to be
 * persistent.
 */
@Entity
@Table(name="external_record_info")
public class ExternalRecordInfo {

    public enum MaterialType {
        SERIAL,
        BOOK,
        SOUND,
        DOCUMENTATION,
        ARCHIVE,
        VISUAL,
        MOVING_VISUAL,
        ARTICLE,
        OTHER
    }

    public enum PublicationStatus {
        UNKNOWN,
        IRSH,
        OPEN,
        RESTRICTED,
        MINIMAL,
        PICTORIGHT,
        CLOSED
    }

    public enum Restriction {
        DATE_RESTRICTED,
        RESTRICTED,
        OPEN,
        CLOSED
    }

    /** The id. */
    @Id
    @GeneratedValue
    @Column(name="id")
    private int id;

    /**
     * Get the info's id.
     * @return the info's id.
     */
    public int getId() {
        return id;
    }

     /** The Record's title. */
    @NotBlank
    @Size(max=125)
    @Column(name="title", nullable=false)
    private String title;

    /**
     * Get the Record's title.
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the Record's title.
     * @param title The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="material_type", nullable=false)
    private MaterialType materialType;

    /**
     * Get external material type.
     * @return The external material type, do not rely on this too much.
     */
    public MaterialType getMaterialType() {
        return materialType;
    }

    /**
     * Se the material type.
     * @param type The material type to set.
     */
    public void setMaterialType(MaterialType type) {
        materialType = type;
    }

    /**
     * Holder of the copyright.
     */
    @Size(max=255)
    @Column(name="copyright")
    private String copyright;

    /**
     * Get the holder of the copyright.
     * @return The holder of the copyright.
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Set the holder of the copyright.
     * @param copyright The holder of the copyright.
     */
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="publication_status", nullable=false)
    private PublicationStatus publicationStatus;

    /**
     * Get the publication status.
     * @return the publication status.
     */
    public PublicationStatus getPublicationStatus() {
            return publicationStatus;
    }

    /**
     * Set the publication status.
     * @param publicationStatus the publication status.
     */
    public void setPublicationStatus(PublicationStatus publicationStatus) {
        this.publicationStatus = publicationStatus;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name="restriction", nullable=false)
    private Restriction restriction;

    /**
     * Get the restriction.
     * @return the restriction.
     */
    public Restriction getRestriction() {
        return restriction;
    }

    /**
     * Set the restriction.
     * @param restriction the restriction.
     */
    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
    }

    @Size(max=125)
    @Column(name="author")
    private String author;

    /**
     * Get the author.
     * @return The author if applicable (i.e. archives do not have an author,
     * but books do).
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Set the author.
     * @param author The value for the author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    @Size(max=30)
    @Column(name="year")
    private String displayYear;

    /**
     * Get the display year, contains all kinds of string characters ("ca."
     * "-" etc.),
     * therefore it is a string instead of an int.
     * @return The year to display.
     */
    public String getDisplayYear() {
        return displayYear;
    }

    /**
     * Set the display year.
     * @param year The year to set for display.
     */
    public void setDisplayYear(String year) {
         displayYear = year;
    }

    @Size(max=255)
    @Column(name="physical_description")
    private String physicalDescription;

    /**
     * Get the physical description.
     * @return the physical description.
     */
    public String getPhysicalDescription() {
        return physicalDescription;
    }

    /**
     * Set the physical description.
     * @param physicalDescription the physical description.
     */
    public void setPhysicalDescription(String physicalDescription) {
        this.physicalDescription = physicalDescription;
    }

    @Size(max=255)
    @Column(name="genres")
    private String genres;

    /**
     * Get the genres.
     * @return the genres.
     */
    public String getGenres() {
        return genres;
    }

    /**
     * Get the genres as a set.
     * @return the genres as a set.
     */
    public Set<String> getGenresSet() {
        return new HashSet<>(StringUtils.commaDelimitedListToSet(genres));
    }

    /**
     * Set the genres.
     * @param genres the genres.
     */
    public void setGenres(String genres) {
        this.genres = genres;
    }

    /** The Record's container. */
    @Size(max=255)
    @Column(name="container")
    private String container;

    /**
     * Get the Record's container.
     * @return The container.
     */
    public String getContainer() {
        return container;
    }

    /**
     * Set the Record's container.
     * @param container The container.
     */
    public void setContainer(String container) {
        this.container = container;
    }

    /**
     * Merge other record's data with this record.
     * @param other The other record.
     */
    public void mergeWith(ExternalRecordInfo other) {
        setTitle(other.getTitle());
        setMaterialType(other.getMaterialType());
        setCopyright(other.getCopyright());
        setPublicationStatus(other.getPublicationStatus());
        setRestriction(other.getRestriction());
        setAuthor(other.getAuthor());
        setDisplayYear(other.getDisplayYear());
        setPhysicalDescription(other.getPhysicalDescription());
        setGenres(other.getGenres());
        setContainer(other.getContainer());
    }
}
