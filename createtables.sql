CREATE TABLE globalreference (
	id serial NOT NULL,
	name varchar(255) NOT NULL,
	identifier varchar(255) NOT NULL,
	type varchar(255) NOT NULL,
	CONSTRAINT globalreference_pk PRIMARY KEY (id),
	CONSTRAINT globalreference_unique UNIQUE(name, identifier)
) WITH (
  OIDS=FALSE
);



CREATE TABLE bookmeta (
	id serial NOT NULL,
	globalreference_id bigint NOT NULL,
	author varchar(255) NOT NULL,
	publisher varchar(255) NOT NULL,
	genre varchar(255) NOT NULL,
	publishingyear int NOT NULL,
	CONSTRAINT bookmeta_pk PRIMARY KEY (id),
	CONSTRAINT bookmeta_unique UNIQUE(globalreference_id)
) WITH (
  OIDS=FALSE
);



CREATE TABLE foodstuffmeta (
	id serial NOT NULL,
	globalreference_id bigint NOT NULL,
	producer varchar(255) NOT NULL,
	calories DECIMAL NOT NULL,
	carbohydrate DECIMAL NOT NULL,
	fat DECIMAL NOT NULL,
	protein DECIMAL NOT NULL,
	CONSTRAINT foodstuffmeta_pk PRIMARY KEY (id),
	CONSTRAINT foodstuffmeta_pk_unique UNIQUE(globalreference_id)
) WITH (
  OIDS=FALSE
);



CREATE TABLE item (
	id serial NOT NULL,
	globalreference_id bigint NOT NULL,
	location varchar(255) NOT NULL,
	date TIMESTAMP NOT NULL,
	expiration TIMESTAMP,
        deleted BOOLEAN NOT NULL DEFAULT 'false',
	CONSTRAINT item_pk PRIMARY KEY (id)
) WITH (
  OIDS=FALSE
);



CREATE TABLE person (
	identifier varchar(255) NOT NULL,
	name varchar(255) NOT NULL,
	email varchar(255),
	password varchar(255) NOT NULL,
	apikey varchar(255),
	date TIMESTAMP NOT NULL,
	deleted BOOLEAN NOT NULL,
	CONSTRAINT person_pk PRIMARY KEY (identifier),
	CONSTRAINT person_unique UNIQUE(name)
) WITH (
  OIDS=FALSE
);



CREATE TABLE permission (
	id serial NOT NULL,
	person_identifier varchar(255) NOT NULL,
	item_id bigint NOT NULL,
	canedit BOOLEAN NOT NULL,
	candelete BOOLEAN NOT NULL,
	CONSTRAINT permission_pk PRIMARY KEY (id),
	CONSTRAINT permission_unique UNIQUE(person_identifier, item_id)
) WITH (
  OIDS=FALSE
);



CREATE TABLE sessioncontrol (
	sessionid varchar(255) NOT NULL,
	person_identifier varchar(255) NOT NULL,
	date TIMESTAMP NOT NULL,
	CONSTRAINT sessioncontrol_pk PRIMARY KEY (sessionid)
) WITH (
  OIDS=FALSE
);



CREATE TABLE bugreport (
	person_identifier varchar(255) NOT NULL,
	subject varchar(255) NOT NULL,
	description TEXT NOT NULL,
	date TIMESTAMP NOT NULL
) WITH (
  OIDS=FALSE
);



CREATE TABLE meal (
	id serial NOT NULL,
	date TIMESTAMP NOT NULL,
	deleted BOOLEAN NOT NULL,
	person_identifier varchar NOT NULL,
	CONSTRAINT meal_pk PRIMARY KEY (id)
) WITH (
  OIDS=FALSE
);



CREATE TABLE mealcomponent (
	id serial NOT NULL,
	meal_id bigint NOT NULL,
	globalreference_id bigint NOT NULL,
	mass DECIMAL NOT NULL,
	CONSTRAINT mealcomponent_pk PRIMARY KEY (id)
) WITH (
  OIDS=FALSE
);


CREATE TABLE recipe (
	id serial NOT NULL,
	name varchar(255) NOT NULL,
	directions TEXT NOT NULL,
	description TEXT NOT NULL,
	type varchar(255) NOT NULL,
	totalmass DECIMAL NOT NULL,
	date TIMESTAMP NOT NULL,
	deleted BOOLEAN NOT NULL,
	CONSTRAINT recipe_pk PRIMARY KEY (id)
) WITH (
  OIDS=FALSE
);


CREATE TABLE ingredient (
	id serial NOT NULL,
	globalreference_id bigint NOT NULL,
	recipe_id bigint NOT NULL,
	mass DECIMAL NOT NULL,
	CONSTRAINT ingredient_pk PRIMARY KEY (id),
	CONSTRAINT ingredient_unique UNIQUE(recipe_id, globalreference_id)
) WITH (
  OIDS=FALSE
);


ALTER TABLE bookmeta ADD CONSTRAINT bookmeta_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id);

ALTER TABLE foodstuffmeta ADD CONSTRAINT foodstuffmeta_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id);

ALTER TABLE item ADD CONSTRAINT item_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id);

ALTER TABLE permission ADD CONSTRAINT permission_fk0 FOREIGN KEY (person_identifier) REFERENCES person(identifier);

ALTER TABLE permission ADD CONSTRAINT permission_fk1 FOREIGN KEY (item_id) REFERENCES item(id);

ALTER TABLE sessioncontrol ADD CONSTRAINT sessioncontrol_fk0 FOREIGN KEY (person_identifier) REFERENCES person(identifier);

ALTER TABLE bugreport ADD CONSTRAINT bugreport_fk0 FOREIGN KEY (person_identifier) REFERENCES person(identifier);

ALTER TABLE meal ADD CONSTRAINT meal_fk0 FOREIGN KEY (person_identifier) REFERENCES person(identifier);

ALTER TABLE mealcomponent ADD CONSTRAINT mealcomponent_fk0 FOREIGN KEY (meal_id) REFERENCES meal(id);

ALTER TABLE mealcomponent ADD CONSTRAINT mealcomponent_fk1 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id);

ALTER TABLE ingredient ADD CONSTRAINT ingredient_fk0 FOREIGN KEY (globalreference_id) REFERENCES globalreference(id);

ALTER TABLE ingredient ADD CONSTRAINT ingredient_fk1 FOREIGN KEY (recipe_id) REFERENCES recipe(id);
