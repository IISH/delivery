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
INSERT INTO authorities VALUES (20, 'View page as an authorized deliver user.', 'ROLE_DELIVERY_USER');
INSERT INTO authorities VALUES (21, 'View actuator', 'ROLE_ACTUATOR');
INSERT INTO authorities VALUES (22, 'View printer', 'ROLE_PRINTER_VIEW');
INSERT INTO authorities VALUES (23, 'Modify printer', 'ROLE_PRINTER_MODIFY');

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

INSERT INTO group_permissions VALUES (1, 04);
INSERT INTO group_permissions VALUES (1, 05);
INSERT INTO group_permissions VALUES (1, 13);
INSERT INTO group_permissions VALUES (1, 14);
INSERT INTO group_permissions VALUES (1, 20);
INSERT INTO group_permissions VALUES (1, 22);
INSERT INTO group_permissions VALUES (1, 23);
INSERT INTO group_permissions VALUES (2, 01);
INSERT INTO group_permissions VALUES (2, 02);
INSERT INTO group_permissions VALUES (2, 03);
INSERT INTO group_permissions VALUES (2, 04);
INSERT INTO group_permissions VALUES (2, 05);
INSERT INTO group_permissions VALUES (2, 06);
INSERT INTO group_permissions VALUES (2, 07);
INSERT INTO group_permissions VALUES (2, 08);
INSERT INTO group_permissions VALUES (2, 09);
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
INSERT INTO group_permissions VALUES (2, 20);
INSERT INTO group_permissions VALUES (2, 21);
INSERT INTO group_permissions VALUES (2, 22);
INSERT INTO group_permissions VALUES (2, 23);
INSERT INTO group_permissions VALUES (3, 04);
INSERT INTO group_permissions VALUES (3, 05);
INSERT INTO group_permissions VALUES (3, 06);
INSERT INTO group_permissions VALUES (3, 07);
INSERT INTO group_permissions VALUES (3, 11);
INSERT INTO group_permissions VALUES (3, 12);
INSERT INTO group_permissions VALUES (3, 13);
INSERT INTO group_permissions VALUES (3, 14);
INSERT INTO group_permissions VALUES (3, 16);
INSERT INTO group_permissions VALUES (3, 17);
INSERT INTO group_permissions VALUES (3, 18);
INSERT INTO group_permissions VALUES (3, 19);
INSERT INTO group_permissions VALUES (3, 20);
INSERT INTO group_permissions VALUES (3, 22);
INSERT INTO group_permissions VALUES (3, 23);
INSERT INTO group_permissions VALUES (4, 02);
INSERT INTO group_permissions VALUES (4, 03);
INSERT INTO group_permissions VALUES (4, 20);
INSERT INTO group_permissions VALUES (4, 22);
INSERT INTO group_permissions VALUES (4, 23);

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--


INSERT INTO users VALUES (1, 'magazijnmedewerker@localhost', '00', 'magazijnmedewerker');
INSERT INTO users VALUES (2, 'admins@localhost', '00', 'admins');
INSERT INTO users VALUES (3, 'infobalie@localhost', '00', 'infobalie');
INSERT INTO users VALUES (4, 'metadatabeheer@localhost', '00', 'metadatabeheer');
INSERT INTO users VALUES (5, 'delivery@localhost', '00', 'delivery');
INSERT INTO users VALUES (6, 'guest@localhost', '00', 'guest');

--
-- Data for Name: user_groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO user_groups SET user_id = 1, group_id = 1;
INSERT INTO user_groups SET user_id = 2, group_id = 2;
INSERT INTO user_groups SET user_id = 3, group_id = 3;
INSERT INTO user_groups SET user_id = 4, group_id = 4;
INSERT INTO user_groups SET user_id = 5, group_id = 2;

