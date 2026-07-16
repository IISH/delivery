#!/bin/bash

# Configuration
# Alle sequenties naar de laatst gebruikte ID met een interval van 50

TABLES=(
'holding_reservations'
'permissions'
'archive_holding_info'
'external_holding_info'
'external_record_info'
'holdings'
'records'
'holding_reproductions'
'reproductions'
'reproduction_custom_notes'
'reproduction_standard_options'
'reservations'
'reservation_date_exceptions'
'authorities'
'groups'
'users')

POSTGRES_DB='delivery'
POSTGRES_USER='delivery'
INCREMENT='50'

for TABLE in "${TABLES[@]}"
do
    SEQ_NAME="${TABLE}_id_seq"
    echo "Processing table: $TABLE (Sequence: $SEQ_NAME)..."

    # Use a single psql call with a transaction block
    psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<EOF
BEGIN;
    -- 1. Create the sequence if it doesn't exist
    CREATE SEQUENCE IF NOT EXISTS ${SEQ_NAME} INCREMENT BY ${INCREMENT};

    -- 2. Link the sequence as the default for the 'id' column
    ALTER TABLE ${TABLE} ALTER COLUMN id SET DEFAULT nextval('${SEQ_NAME}');

    -- 3. Mark the sequence as owned by this table (crucial for pg_dump)
    ALTER SEQUENCE ${SEQ_NAME} OWNED BY ${TABLE}.id;

    -- 4. Sync sequence value to the current max(id) to avoid collisions
    SELECT setval('${SEQ_NAME}', coalesce(max(id), 1)) FROM ${TABLE};
COMMIT;
EOF

[ $? -eq 0 ] && echo "Successfully linked $SEQ_NAME to $TABLE." || echo "ERROR!!!!!!!!!!!!!!!!!!!!!: Failed to process $TABLE."

done


# Gooi oude tabellen weg die we niet meer gebruiken:
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "DROP TABLE recordpermissions_old"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "DROP TABLE permissions_old"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "DROP SEQUENCE hibernate_sequence"

# OpenID
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "ALTER TABLE users ADD email VARCHAR(255) NOT NULL DEFAULT 'user@localhost';"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "ALTER TABLE users ADD sub VARCHAR(255) NOT NULL  DEFAULT '00000';"

# todo: voor iedere bestaande gebruiker inloggen en deze vul de admin de juiste rechten toekennen.
# De oude accounts mogen in de prullenbak.

# Een nieuwe groep: developer

# Nieuwe rol voor aanschouwen van de home page.
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO authorities VALUES (20, 'View page as an authorized deliver user.', 'ROLE_DELIVERY_USER');"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO authorities VALUES (21, 'View actuator', 'ROLE_ACTUATOR');"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO authorities VALUES (22, 'View printer', 'ROLE_PRINTER_VIEW');"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO authorities VALUES (23, 'Modify printer', 'ROLE_PRINTER_MODIFY');"

# All groups are delivery users
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (1, 20);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (2, 20);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (3, 20);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (4, 20);"

# View printer
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (1, 22);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (2, 22);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (3, 22);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (4, 22);"

# Modify printer
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (1, 23);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (2, 23);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (3, 23);"
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (4, 23);"

# Nieuwe groep: developers
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO groups VALUES (5, 'Developers', 'Monitoring en onderhoud');"

# Actuator for developers
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "INSERT INTO group_permissions VALUES (5, 21);"


# year is een reserved field. Wordt display_year
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "ALTER TABLE external_record_info RENAME COLUMN year TO display_year;"
