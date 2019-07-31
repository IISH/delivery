--
-- Data for Name: authorities; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO authorities VALUES (1, 'Modify users and authorities.', 'ROLE_USER_MODIFY');
INSERT INTO authorities VALUES (2, 'Modify record metadata.', 'ROLE_RECORD_MODIFY');
INSERT INTO authorities VALUES (3, 'View record contact data.', 'ROLE_RECORD_CONTACT_VIEW');
INSERT INTO authorities VALUES (4, 'View reservations.', 'ROLE_RESERVATION_VIEW');
INSERT INTO authorities VALUES (5, 'Modify reservations.', 'ROLE_RESERVATION_MODIFY');
INSERT INTO authorities VALUES (6, 'View permissions.', 'ROLE_PERMISSION_VIEW');
INSERT INTO authorities VALUES (7, 'Modify permissions.', 'ROLE_PERMISSION_MODIFY');
INSERT INTO authorities VALUES (8, 'Delete permissions.', 'ROLE_PERMISSION_DELETE');
INSERT INTO authorities VALUES (9, 'Delete reservations.', 'ROLE_RESERVATION_DELETE');
INSERT INTO authorities VALUES (10, 'Delete records.', 'ROLE_RECORD_DELETE');
INSERT INTO authorities VALUES (11, 'Create reservations.', 'ROLE_RESERVATION_CREATE');
INSERT INTO authorities VALUES (12, 'Create reproductions.', 'ROLE_REPRODUCTION_CREATE');
INSERT INTO authorities VALUES (13, 'View reproductions.', 'ROLE_REPRODUCTION_VIEW');
INSERT INTO authorities VALUES (14, 'Modify reproductions.', 'ROLE_REPRODUCTION_MODIFY');
INSERT INTO authorities VALUES (15, 'Delete reproductions.', 'ROLE_REPRODUCTION_DELETE');
INSERT INTO authorities VALUES (16, 'View date exceptions.', 'ROLE_DATE_EXCEPTION_VIEW');
INSERT INTO authorities VALUES (17, 'Create date exceptions.', 'ROLE_DATE_EXCEPTION_CREATE');
INSERT INTO authorities VALUES (18, 'Modify date exceptions.', 'ROLE_DATE_EXCEPTION_MODIFY');
INSERT INTO authorities VALUES (19, 'Delete date exceptions.', 'ROLE_DATE_EXCEPTION_DELETE');

--
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO groups VALUES (1, 'Magazijnmedewerkers', 'Levering');
INSERT INTO groups VALUES (2, 'Admins', 'Administrator');
INSERT INTO groups VALUES (3, 'Infobalie', 'Infobalie');
INSERT INTO groups VALUES (4, 'Metadata beheer', 'Metadata beheer');

--
-- Data for Name: group_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO group_permissions VALUES (2, 1);
INSERT INTO group_permissions VALUES (2, 2);
INSERT INTO group_permissions VALUES (2, 3);
INSERT INTO group_permissions VALUES (2, 4);
INSERT INTO group_permissions VALUES (2, 5);
INSERT INTO group_permissions VALUES (2, 6);
INSERT INTO group_permissions VALUES (2, 7);
INSERT INTO group_permissions VALUES (2, 8);
INSERT INTO group_permissions VALUES (2, 9);
INSERT INTO group_permissions VALUES (2, 10);
INSERT INTO group_permissions VALUES (2, 11);
INSERT INTO group_permissions VALUES (2, 12);
INSERT INTO group_permissions VALUES (2, 13);
INSERT INTO group_permissions VALUES (2, 14);
INSERT INTO group_permissions VALUES (2, 15);
INSERT INTO group_permissions VALUES (2, 16);
INSERT INTO group_permissions VALUES (2, 17);
INSERT INTO group_permissions VALUES (2, 18);
INSERT INTO group_permissions VALUES (2, 19);
INSERT INTO group_permissions VALUES (1, 4);
INSERT INTO group_permissions VALUES (1, 5);
INSERT INTO group_permissions VALUES (3, 7);
INSERT INTO group_permissions VALUES (3, 6);
INSERT INTO group_permissions VALUES (3, 11);
INSERT INTO group_permissions VALUES (3, 4);
INSERT INTO group_permissions VALUES (3, 5);
INSERT INTO group_permissions VALUES (4, 2);
INSERT INTO group_permissions VALUES (4, 3);

INSERT INTO group_permissions VALUES (1, 13);
INSERT INTO group_permissions VALUES (1, 14);

INSERT INTO group_permissions VALUES (3, 12);
INSERT INTO group_permissions VALUES (3, 13);
INSERT INTO group_permissions VALUES (3, 14);

INSERT INTO group_permissions VALUES (3, 16);
INSERT INTO group_permissions VALUES (3, 17);
INSERT INTO group_permissions VALUES (3, 18);
INSERT INTO group_permissions VALUES (3, 19);

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO users VALUES (2, 'lwo@iisg.nl');
INSERT INTO users VALUES (3, 'etu@iisg.nl');
INSERT INTO users VALUES (4, 'kerim');

--
-- Data for Name: user_groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO user_groups VALUES (2, 2);
INSERT INTO user_groups VALUES (3, 2);
INSERT INTO user_groups VALUES (4, 2);

ALTER SEQUENCE HIBERNATE_SEQUENCE RESTART WITH 100
