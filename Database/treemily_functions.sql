-- Functions

CREATE OR REPLACE FUNCTION isallowedtoseeentity(entityid integer, profileid integer) RETURNS boolean AS $$
        BEGIN
        		vis := select visibility from accesscontrolledentity where id = entityid;
                IF vis = 0 then return true
                else if vis = 1
                else
        END;
$$ LANGUAGE plpgsql;