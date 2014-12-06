package liquibase.change;

import liquibase.changelog.ChangeSet;
import liquibase.exception.SetupException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;

public interface Change extends LiquibaseSerializable {

    /**
     * This method will be called by the changlelog parsing process after all of the
     * properties have been set to allow the task to do any additional initialization logic.
     */
    void finishInitialization() throws SetupException;

    /**
     * Returns the changeSet this Change is part of. Will return null if this instance was not constructed as part of a changelog file.
     */
    ChangeSet getChangeSet();

    /**
     * Sets the changeSet this Change is a part of. Called automatically by Liquibase during the changelog parsing process.
     */
    void setChangeSet(ChangeSet changeSet);

    /**
     * Sets the {@link ResourceAccessor} that should be used for any file and/or resource loading needed by this Change.
     * Called automatically by Liquibase during the changelog parsing process.
     */
    void setResourceAccessor(ResourceAccessor resourceAccessor);

    /**
     * Calculates the checksum of this Change based on the current configuration.
     * The checksum should take into account all settings that would impact what actually happens to the database
     * and <b>NOT</b> include any settings that do not impact the actual execution of the change.
     */
    CheckSum generateCheckSum();

}
