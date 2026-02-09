CREATE TABLE persistable_multi_polygon (
    id bigint NOT NULL,
    multi_polygon geometry
);

ALTER TABLE persistable_multi_polygon OWNER TO tiamat;

ALTER TABLE ONLY persistable_multi_polygon
    ADD CONSTRAINT persistable_multi_polygon_pkey PRIMARY KEY (id);

CREATE SEQUENCE persistable_multi_polygon_seq
    START WITH 1
    INCREMENT BY 10
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE persistable_multi_polygon_seq OWNER TO tiamat;

ALTER TABLE access_space ADD COLUMN multi_surface_id bigint;
ALTER TABLE access_space ADD CONSTRAINT fk_access_space_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE boarding_position ADD COLUMN multi_surface_id bigint;
ALTER TABLE boarding_position ADD CONSTRAINT fk_boarding_position_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE equipment_place ADD COLUMN multi_surface_id bigint;
ALTER TABLE equipment_place ADD CONSTRAINT fk_equipment_place_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE parking ADD COLUMN multi_surface_id bigint;
ALTER TABLE parking ADD CONSTRAINT fk_parking_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE parking_area ADD COLUMN multi_surface_id bigint;
ALTER TABLE parking_area ADD CONSTRAINT fk_parking_area_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE quay ADD COLUMN multi_surface_id bigint;
ALTER TABLE quay ADD CONSTRAINT fk_quay_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE stop_place ADD COLUMN multi_surface_id bigint;
ALTER TABLE stop_place ADD CONSTRAINT fk_stop_place_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE tariff_zone ADD COLUMN multi_surface_id bigint;
ALTER TABLE tariff_zone ADD CONSTRAINT fk_tariff_zone_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE topographic_place ADD COLUMN multi_surface_id bigint;
ALTER TABLE topographic_place ADD CONSTRAINT fk_topographic_place_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);

ALTER TABLE fare_zone ADD COLUMN multi_surface_id bigint;
ALTER TABLE fare_zone ADD CONSTRAINT fk_fare_zone_multi_surface FOREIGN KEY (multi_surface_id) REFERENCES persistable_multi_polygon(id);
