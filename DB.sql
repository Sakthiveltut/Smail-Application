create database if not exists email;
use email;

show tables;

select * from Users;
select count(*) from Folders;
select * from Folders;
select * from UserFolders;
select * from Messages;
select * from users where email = "mosesh@gmail.com";

create table Users (
	id bigint auto_increment primary key,
	name varchar(50) not null,
    email varchar(255) unique not null,
    password varchar(255) not null, 
    login_time datetime
);

create table Folders(
	id tinyint auto_increment primary key,
    name varchar(20) unique not null,
	created_time timestamp default current_timestamp
);
insert into Folders(name) values('inbox'),('send'),('draft'),('spam'),('bin');

create table UserFolders(	
	created_by bigint not null,
    folder_id tinyint not null,
    foreign key (created_by) references Users(id),
    foreign key (folder_id) references Folders(id),
    primary key(created_by,folder_id)
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
	foreign key (user_id) references Users(id) ON DELETE restrict,
	foreign key (message_id) references Messages(id) ON DELETE restrict,
	foreign key (folder_id) references Folders(id) ON DELETE restrict
);

create table Recipients(
	message_id bigint not null,
	user_id bigint not null,
    type enum('to','cc'),
    foreign key (message_id) references Messages(id) ON DELETE restrict,
    foreign key (user_id) references Users(id) ON DELETE restrict
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
);

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





drop database if exists email;
drop table if exists Users;
drop table if exists Recipients;
drop table if exists Messages;
drop table if exists Attachments;
truncate table Users;

set sql_safe_updates = 0;
delete from Users;
set sql_safe_updates = 1;

