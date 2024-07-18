create database if not exists email;
use email;

create table Users(
	id bigint auto_increment primary key,
	email varchar(254) unique not null
);
create table RegisteredUsers(
	user_id bigint not null,
	name varchar(50) not null,
	password varchar(255) not null, 
    login_time datetime,
    foreign key (user_id) references Users(id) ON DELETE restrict
);	
create table Folders(
	id tinyint auto_increment primary key,
    name varchar(20) unique not null,
	created_time timestamp default current_timestamp
);
create table UserFolders(	
	user_id bigint not null,
    folder_id tinyint not null,
	unique key(user_id,folder_id),
    foreign key (user_id) references Users(id) ON DELETE restrict,
    foreign key (folder_id) references Folders(id) ON DELETE restrict
);
create table Messages(
	id bigint auto_increment primary key,
	sender_id bigint not null,
	subject varchar(255) not null,
	description text,
    has_attachment boolean default false,
    created_time timestamp default current_timestamp,
    foreign key (sender_id) references Users(id) ON DELETE restrict
);
create table MessageFolders(
    user_id bigint not null,
    folder_id tinyint not null,
	message_id bigint not null,
	is_starred boolean default false,
	is_read boolean default false,
    unique key(user_id,message_id),
	foreign key (user_id) references Users(id) ON DELETE restrict,
	foreign key (message_id) references Messages(id) ON DELETE restrict,
	foreign key (folder_id) references Folders(id) ON DELETE restrict
);
create table RecipientTypes(
	id tinyint auto_increment primary key,
	type varchar(5),
    check (type in ('to','cc'))
);
create table Recipients(
	message_id bigint not null,
	user_id bigint not null,
    type_id tinyint not null,
    unique key(user_id,message_id,type_id),
    foreign key (message_id) references Messages(id) ON DELETE restrict,
    foreign key (user_id) references Users(id) ON DELETE restrict,
    foreign key (type_id) references RecipientTypes(id) ON DELETE restrict
);
create table FileTypes(
	id tinyint auto_increment primary key,
    type varchar(10)
);
create table Attachments(
	id int auto_increment primary key,
    message_id bigint not null,
    name varchar(255) not null,
    type_id tinyint not null,
    size bigint unsigned,	
    path varchar(255) not null,
    unique key(message_id,name,path),
    foreign key (message_id) references Messages(id) ON DELETE restrict,
    foreign key (type_id) references FileTypes(id) ON DELETE restrict
);
insert into Folders(name) values('inbox'),('sent'),('draft'),('spam'),('bin');
insert into RecipientTypes(type) values('to'),('cc');
INSERT INTO FileTypes (type) VALUES 
    ('pdf'), ('doc'), ('docx'), ('xls'), ('xlsx'), ('ppt'), ('pptx'), 
    ('txt'), ('rtf'), ('jpg'), ('jpeg'), ('png'), ('gif'), 
    ('mp3'), ('mp4'), ('mkv'), ('zip'), ('rar'), ('7z'), 
    ('gz'), ('tar'), ('tar.gz'); 

#---------------------------------------------------------------------------
DELETE Recipients
FROM Recipients
JOIN RecipientTypes ON Recipients.type_id = RecipientTypes.id
WHERE Recipients.message_id = 5 AND RecipientTypes.type = 'to';

				DELETE A
				FROM Attachments A
				JOIN MessageFolders MF ON A.message_id = MF.message_id
				WHERE MF.user_id = ?
				  AND A.message_id = ?
				  AND A.id = ?
                  AND MF.folder_id = ?;

select * from Users u join RegisteredUsers ru  on u.id = ru.user_id where u.email = "sakthi@smail.com" and ru.password = "Sakthi@123";

select * from Users u join RegisteredUsers ru  on u.id = ru.user_id where u.id=4;

show tables;

select * from Recipients;
select * from RegisteredUsers;
select count(*) from Folders;
select * from Folders;
select * from UserFolders;
select * from MessageFolders;
select * from Messages;
select * from users ;


insert into Users(email) values("sakthi@smail.com");
insert into Users(email) values("mosesh@smail.com");
insert into Users(email) values("rahul@smail.com");
select * from Users;

select * from Folders;

insert into UserFolders values(1,2);
insert into UserFolders values(1,1);
insert into UserFolders values(2,1);
insert into UserFolders values(2,2);
insert into UserFolders values(3,1);
insert into UserFolders values(3,2);
select * from UserFolders;

insert into Messages(sender_id,subject,description) values(1,"sakthi","sakthi");
insert into Messages(sender_id,subject,description) values(2,"mosesh","mosesh");
insert into Messages(sender_id,subject,description) values(3,"rahul","rahul");
select * from Messages;

insert into MessageFolders values(1,2,1);
insert into MessageFolders values(2,1,1);
insert into MessageFolders values(3,1,1);

insert into MessageFolders values(2,2,2);
insert into MessageFolders values(1,1,2);
insert into MessageFolders values(3,1,2);

insert into MessageFolders values(3,2,3);
insert into MessageFolders values(2,1,3);
insert into MessageFolders values(1,1,3);
#-----------------------------------------

insert into MessageFolders values(2,1);
insert into MessageFolders values(1,1);
insert into MessageFolders values(1,1);

insert into MessageFolders values(2,2);
insert into MessageFolders values(1,2);
#insert into MessageFolders values(1,2);

insert into MessageFolders values(2,3);
insert into MessageFolders values(1,3);
#insert into MessageFolders values(1,3);

select * from MessageFolders;

insert into Recipients values(1,2,'to');
insert into Recipients values(1,3,'cc');

insert into Recipients values(2,1,'to');
insert into Recipients values(2,3,'cc');

insert into Recipients values(3,2,'to');
insert into Recipients values(3,1,'cc');
select * from Recipients;

SELECT 
    m.id AS message_id,
    m.subject,
    m.description,
    m.is_starred,
    m.is_read,
    m.has_attachment,
    m.created_time,
    r.type AS recipient_type,
    u.email AS recipient_email,
    a.name AS attachment_name,
    a.path AS attachment_path,
    ft.type AS attachment_type
FROM 
    Messages m
JOIN 
    Recipients rc ON m.id = rc.message_id
JOIN 
    Users u ON rc.user_id = u.id
JOIN 
    RecipientTypes r ON rc.type_id = r.id
LEFT JOIN 
    Attachments a ON m.id = a.message_id
LEFT JOIN 
    FileTypes ft ON a.type_id = ft.id
JOIN 
    MessageFolders mf ON m.id = mf.message_id
WHERE 
    mf.user_id = 2
    AND mf.folder_id = 1;
    
    
SELECT 
    m.id AS message_id,
    m.sender_id,
    m.subject,
    m.description,
    m.is_starred,
    m.is_read,
    m.has_attachment,
    m.created_time,
    GROUP_CONCAT(DISTINCT CONCAT(rt.type, ': ', u.email) SEPARATOR ', ') AS recipients,
    MAX(a.name) AS attachment_name,
    MAX(a.path) AS attachment_path,
    MAX(ft.type) AS attachment_type
FROM 
    Messages m
LEFT JOIN 
    Recipients r ON m.id = r.message_id
LEFT JOIN 
    Users u ON r.user_id = u.id
LEFT JOIN 
    RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN 
    Attachments a ON m.id = a.message_id
LEFT JOIN 
    FileTypes ft ON a.type_id = ft.id
GROUP BY 
    m.id, m.sender_id, m.subject, m.description, m.is_starred, m.is_read, m.has_attachment, m.created_time;


SELECT 
    m.id AS message_id,
    m.sender_id,
    m.subject,
    m.description,
    m.is_starred,
    m.is_read,
    m.has_attachment,
    m.created_time,
    GROUP_CONCAT(DISTINCT CONCAT(rt.type, ': ', u.email) SEPARATOR ', ') AS recipients,
    MAX(a.name) AS attachment_name,
    MAX(a.path) AS attachment_path,
    MAX(ft.type) AS attachment_type
FROM 
    Messages m
LEFT JOIN 
    Recipients r ON m.id = r.message_id
LEFT JOIN 
    Users u ON r.user_id = u.id
LEFT JOIN 
    RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN 
    Attachments a ON m.id = a.message_id
LEFT JOIN 
    FileTypes ft ON a.type_id = ft.id
JOIN 
    MessageFolders mf ON m.id = mf.message_id
WHERE 
    mf.user_id = 1
    AND mf.folder_id = 2
GROUP BY 
    m.id, m.sender_id, m.subject, m.description, m.is_starred, m.is_read, m.has_attachment, m.created_time;


SELECT 
    m.id AS message_id,
    m.sender_id,
    su.email AS sender_email, 
    m.subject,
    m.description,
    m.is_starred,
    m.is_read,
    m.has_attachment,
    m.created_time,
    GROUP_CONCAT(DISTINCT CONCAT(rt.type, ': ', ru.email) SEPARATOR ', ') AS recipients, 
    MAX(a.name) AS attachment_name,
    MAX(a.path) AS attachment_path,
    MAX(ft.type) AS attachment_type
FROM 
    Messages m
LEFT JOIN 
    Recipients r ON m.id = r.message_id
LEFT JOIN 
    Users ru ON r.user_id = ru.id  
LEFT JOIN 
    RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN 
    Attachments a ON m.id = a.message_id
LEFT JOIN 
    FileTypes ft ON a.type_id = ft.id
JOIN 
    MessageFolders mf ON m.id = mf.message_id
JOIN 
    Users su ON m.sender_id = su.id  
WHERE 
    mf.user_id = 1
    AND mf.folder_id = 1
GROUP BY 
    m.id, m.sender_id, su.email, m.subject, m.description, m.is_starred, m.is_read, m.has_attachment, m.created_time;


SELECT 
    m.id AS message_id,
    m.sender_id,
    su.email AS sender_email,
    m.subject,
    m.description,
    m.is_starred,
    m.is_read,
    m.has_attachment,
    m.created_time,
    CONCAT(
        'to:', GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', '),
        IF(COUNT(DISTINCT CASE WHEN rt.type = 'cc' THEN u.email END) > 0, CONCAT(', cc:', GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ')), '')
    ) AS recipients,
    MAX(a.name) AS attachment_name,
    MAX(a.path) AS attachment_path,
    MAX(ft.type) AS attachment_type
FROM 
    Messages m
LEFT JOIN 
    Recipients r ON m.id = r.message_id
LEFT JOIN 
    Users u ON r.user_id = u.id
LEFT JOIN 
    RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN 
    Attachments a ON m.id = a.message_id
LEFT JOIN 
    FileTypes ft ON a.type_id = ft.id
JOIN 
    MessageFolders mf ON m.id = mf.message_id
JOIN 
    Users su ON m.sender_id = su.id
WHERE 
    mf.user_id = 1
    AND mf.folder_id = 2
GROUP BY 
    m.id, m.sender_id, su.email, m.subject, m.description, m.is_starred, m.is_read, m.has_attachment, m.created_time;


SELECT 
    m.id AS message_id,
    m.sender_id,
    su.email AS sender_email,
    m.subject,
    m.description,
    m.is_starred,
    m.is_read,
    m.has_attachment,
    m.created_time,
    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients,
    MAX(a.name) AS attachment_name,
    MAX(a.path) AS attachment_path,
    MAX(ft.type) AS attachment_type
FROM 
    Messages m
LEFT JOIN 
    Recipients r ON m.id = r.message_id
LEFT JOIN 
    Users u ON r.user_id = u.id
LEFT JOIN 
    RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN 
    Attachments a ON m.id = a.message_id
LEFT JOIN 
    FileTypes ft ON a.type_id = ft.id
JOIN 
    MessageFolders mf ON m.id = mf.message_id
JOIN 
    Users su ON m.sender_id = su.id
WHERE 
    mf.user_id = 1
    AND mf.folder_id = 2
GROUP BY 
    m.id, m.sender_id, su.email, m.subject, m.description, m.is_starred, m.is_read, m.has_attachment, m.created_time;


SELECT 
    m.id,
    su.email AS sender_email,
    m.subject,
    m.description,
    m.is_read,
    mf.is_starred,
    m.created_time,
    GROUP_CONCAT(CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
    GROUP_CONCAT(CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients
FROM 
    Messages m
LEFT JOIN 
    Recipients r ON m.id = r.message_id
LEFT JOIN 
    Users u ON r.user_id = u.id
LEFT JOIN 
    RecipientTypes rt ON r.type_id = rt.id
JOIN 
    MessageFolders mf ON m.id = mf.message_id
JOIN 
    Users su ON m.sender_id = su.id
JOIN 
    Folders f ON mf.folder_id = f.id
WHERE 
    mf.user_id = 1
    AND f.name = "send" 
GROUP BY 
    m.id;
    
SELECT
    m.id,
    su.email AS sender_email,
    m.subject,
    m.description,
    mf.is_read,
 	mf.is_starred,
    m.has_attachment,
    m.created_time,
    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients,
    MAX(a.name) AS attachment_name,
    MAX(a.path) AS attachment_path,
    MAX(ft.type) AS attachment_type
FROM
    Messages m
LEFT JOIN
    Recipients r ON m.id = r.message_id
LEFT JOIN
    Users u ON r.user_id = u.id
LEFT JOIN
    RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN
    Attachments a ON m.id = a.message_id
LEFT JOIN
    FileTypes ft ON a.type_id = ft.id
JOIN
    MessageFolders mf ON m.id = mf.message_id
JOIN
    Users su ON m.sender_id = su.id
JOIN
    Folders f ON mf.folder_id = f.id
WHERE
    mf.user_id = 1
    AND f.name != "inbox"
	AND (m.subject LIKE '%101%' OR m.description LIKE '%101%') GROUP BY m.id;
    
    
	SELECT 
	m.id,
	su.email AS sender_email,
	m.subject,
	m.description,
	mf.is_read,
	mf.is_starred,
	m.created_time,
	m.has_attachment,
	GROUP_CONCAT(CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
	GROUP_CONCAT(CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients,
	MAX(a.name) AS attachment_name,
	MAX(a.path) AS attachment_path,
	MAX(ft.type) AS attachment_type
FROM 
	Messages m
LEFT JOIN 
	Recipients r ON m.id = r.message_id
LEFT JOIN 
	Users u ON r.user_id = u.id
LEFT JOIN 
	RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN 
	Attachments a ON m.id = a.message_id
LEFT JOIN 
	FileTypes ft ON a.type_id = ft.id
JOIN 
	MessageFolders mf ON m.id = mf.message_id
JOIN 
	Users su ON m.sender_id = su.id
JOIN 
	Folders f ON mf.folder_id = f.id
WHERE 
	mf.user_id = 1
     AND m.id =  71;
     
SELECT 
    m.id,
    su.email AS sender_email,
    m.subject,
    m.description,
    mf.is_read,
    mf.is_starred,
    m.created_time,
    m.has_attachment,
    GROUP_CONCAT(distinct CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
    GROUP_CONCAT(distinct CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients,
	GROUP_CONCAT(a.id SEPARATOR ', ') AS attachment_ids,
    GROUP_CONCAT(a.name SEPARATOR ', ') AS attachment_names,
    GROUP_CONCAT(a.path SEPARATOR ', ') AS attachment_paths,
    GROUP_CONCAT(ft.type SEPARATOR ', ') AS attachment_types
FROM 
    Messages m
LEFT JOIN 
    Recipients r ON m.id = r.message_id
LEFT JOIN 
    Users u ON r.user_id = u.id
LEFT JOIN 
    RecipientTypes rt ON r.type_id = rt.id
LEFT JOIN 
    Attachments a ON m.id = a.message_id
LEFT JOIN 
    FileTypes ft ON a.type_id = ft.id
JOIN 
    MessageFolders mf ON m.id = mf.message_id
JOIN 
    Users su ON m.sender_id = su.id
JOIN 
    Folders f ON mf.folder_id = f.id
WHERE 
    mf.user_id = 1
    AND m.id = 73 AND a.id = 1
GROUP BY 
    m.id
ORDER BY 
    m.created_time DESC;
    
			SELECT 
			    m.id,
			    su.email AS sender_email,
			    m.subject,
			    m.description,
			    mf.is_read,
			 	mf.is_starred,
			    m.created_time,
			    m.has_attachment,
			    a.path,
			    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
			    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients,
			    GROUP_CONCAT(a.id SEPARATOR ', ') AS attachment_ids,
			    GROUP_CONCAT(a.name SEPARATOR ', ') AS attachment_names,
			    GROUP_CONCAT(a.path SEPARATOR ', ') AS attachment_paths,
			    GROUP_CONCAT(ft.type SEPARATOR ', ') AS attachment_types
			FROM 
			    Messages m
			LEFT JOIN 
			    Recipients r ON m.id = r.message_id
			LEFT JOIN 
			    Users u ON r.user_id = u.id
			LEFT JOIN 
			    RecipientTypes rt ON r.type_id = rt.id
			LEFT JOIN 
				Attachments a ON m.id = a.message_id
			LEFT JOIN 
			 	FileTypes ft ON a.type_id = ft.id
			JOIN 
			    MessageFolders mf ON m.id = mf.message_id
			JOIN 
			    Users su ON m.sender_id = su.id
			JOIN 
			    Folders f ON mf.folder_id = f.id
			WHERE 
			    mf.user_id = 1 AND m.id = 75 AND a.id = 19
			GROUP BY 
				m.id;

    

set sql_safe_updates = 0;
select * from Users;
select * from RegisteredUsers;
delete from Users;
delete from RegisteredUsers;
set sql_safe_updates = 1;

drop database if exists email;
drop table if exists Users;
drop table if exists RegisteredUsers;
drop table if exists Users;
drop table if exists AnonymousUsers;
drop table if exists Recipients;
drop table if exists Messages;
drop table if exists Attachments;
truncate table Users;


/*create table Users (
	id BigInt auto_increment primary key,
	name varchar(50) not null,
    email varchar(255) unique not null,
    password varchar(255) not null, 
    login_time datetime
    );
    
create table Folders(
	id BigInt auto_increment primary key,
	user_id int not null,
    name varchar(20) not null,
    unique key(user_id, name),
    foreign key (user_id) references Users(id) ON DELETE restrict
    );
    
create table Messages(
	id BigInt auto_increment primary key,
	user_id int not null,
	folder_id int not null,
    `from` varchar(255) not null,
    `to` varchar(255) not null,
    cc varchar(100) not null,
    subject varchar(255) not null,
    description text,
    is_starred boolean default false,
    is_read boolean default false,
    is_attachment boolean default false,
    created_time timestamp default current_timestamp,
    foreign key (user_id) references Users(id) ON DELETE restrict,
    foreign key (folder_id) references Folders(id) ON DELETE restrict
    );
    
create table Attachments(
	id int auto_increment primary key,
    message_id int not null,
    name varchar(255) unique not null,
    type varchar(50),
    size tinyint unsigned,
    path varchar(255) not null,
    unique key(message_id,name),
    foreign key (message_id) references Messages(id) ON DELETE restrict
    );

*/


/*create table Users (
	id bigint auto_increment primary key,
	name varchar(50) not null,
    email varchar(255) unique not null,
    password varchar(255) not null, 
    login_time datetime
);
create table AnonymousUsers(
	id bigint auto_increment primary key,
	email varchar(255) unique not null,
    created_time timestamp default current_timestamp
);
create table Folders(
	id tinyint auto_increment primary key,
    name varchar(20) unique not null,
	created_time timestamp default current_timestamp
);
create table UserFolders(	
	created_by bigint not null,
    folder_id tinyint not null,
	unique key(created_by,folder_id),
    foreign key (created_by) references Users(id) ON DELETE restrict,
    foreign key (folder_id) references Folders(id) ON DELETE restrict
);
create table Messages(
	id bigint auto_increment primary key,
	sender_id bigint not null,
	subject varchar(255) not null,
	description text,
    is_starred boolean default false,
    is_read boolean default false,
    is_attachment boolean default false,
    created_time timestamp default current_timestamp,
    foreign key (sender_id) references Users(id) ON DELETE restrict
);
create table MessageFolders(
    user_id bigint not null,
    folder_id tinyint not null,
	message_id bigint not null,
    unique key(user_id,folder_id,message_id),
	foreign key (user_id) references Users(id) ON DELETE restrict,
	foreign key (message_id) references Messages(id) ON DELETE restrict,
	foreign key (folder_id) references Folders(id) ON DELETE restrict
);
create table Recipients(
	message_id bigint not null,
	user_id bigint not null,
    type enum('to','cc'),
    unique key(user_id,message_id,type),
    foreign key (message_id) references Messages(id) ON DELETE restrict,
    foreign key (user_id) references Users(id) ON DELETE restrict
);
create table AnonymousRecipients(
	message_id bigint not null,
	user_id bigint not null,
    type enum('to','cc'),
	unique key(user_id,message_id,type),
    foreign key (message_id) references Messages(id) ON DELETE restrict,
    foreign key (user_id) references AnonymousUsers(id) ON DELETE restrict
);
create table Attachments(
	id int auto_increment primary key,
    message_id bigint not null,
    name varchar(255) unique not null,
    type varchar(50),
    size int unsigned,
    path varchar(255) not null,
    unique key(message_id,name),
    foreign key (message_id) references Messages(id) ON DELETE restrict
);*/
#----------------------------------------------------------


