import mysql.connector
from datetime import datetime
import random
db_config = {
    "host": "localhost",
    "user": "root",
    "password": "parthsarth9541",
    "database": "chatdb"
}
db_checker_config = {
    "host": "localhost",
    "user": "root",
    "password": "parthsarth9541",
    "database": "chatdb"
}
tables = {
   "users": '''CREATE TABLE IF NOT EXISTS users (
    email VARCHAR(255) PRIMARY KEY,
    password_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);''',

"messages":'''CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender VARCHAR(50),
    room VARCHAR(50),
    content TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);''',

"sessions":'''CREATE TABLE IF NOT EXISTS sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pseudonym VARCHAR(50),
    email VARCHAR(255),
    room varchar(50),
    login_time TIMESTAMP,
    logout_time TIMESTAMP, 
    FOREIGN KEY (email) REFERENCES users(email),
    FOREIGN KEY (room) REFERENCES rooms(name)

);''',
"rooms":'''CREATE TABLE IF NOT EXISTS rooms (
    name VARCHAR(50) PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);''',  "admins": '''CREATE TABLE IF NOT EXISTS admins (
    email VARCHAR(255) PRIMARY KEY,
    password_hash CHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);'''
}
adjective = ['happy', 'sad', 'big', 'small', 'tall', 'short', 'red', 'blue', 'good', 'bad', 'fast', 'slow', 'beautiful', 'ugly', 'kind', 'cruel', 'strong', 'weak', 'old', 'new']
noun = ['lion', 'tiger', 'monkey', 'giraffe', 'kangaroo', 'koala', 'bear', 'hare', 'rabbit', 'cat', 'dog', 'hippo', 'rhino','trex', 'quail']
def make_db():
    conn = mysql.connector.connect(**db_checker_config)
    cursor=conn.cursor()
    cursor.execute("CREATE DATABASE IF NOT EXISTS chatdb;")
    conn.commit()
    cursor.close()
    conn.close()
def make_tables():
    conn =  mysql.connector.connect(**db_config)
    cursor=conn.cursor()
    for i in tables:
        cursor.execute(tables[i])
    conn.commit()
    cursor.close()
    conn.close()
def get_db_connection():
    make_db()
    make_tables()
    return mysql.connector.connect(**db_config)
def create_user(username):
    conn = get_db_connection()
    cursor = conn.cursor()
    result=verify_user(username=username)
    if (result):
        conn.close()
        return result
    else:
        cursor.execute("INSERT INTO users (email) VALUES (%s)", (username, ))
        conn.commit()
        cursor.execute("SELECT * FROM users WHERE email=%s", (username, ))
        result = cursor.fetchone()
    conn.close()
    return result
    

def verify_user(username):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE email=%s", (username,))
    result = cursor.fetchone()
    conn.close()
    return result is not None
def verify_admin(username):
    print("1")
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM admins WHERE email=%s", (username,))
    result = cursor.fetchone()
    conn.close()
    return result is not None
def log_message(username, room, content):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("INSERT INTO messages (sender, room, content) VALUES (%s, %s, %s)", (username, room, content))
    conn.commit()
    conn.close()
def start_session(username):
    conn = get_db_connection()
    cursor = conn.cursor()
    random.shuffle(adjective)
    random.shuffle(noun)
    pseudonym = adjective[1]+'_'+noun[2]+str(random.randint(100,999))
    print(pseudonym)
    """cursor.execute("SELECT COUNT(*) FROM sessions WHERE pseudonym=%s", (pseudonym, ))
    count = cursor.fetchall()
    print(count)
    while (count):
        random.shuffle(adjective)
        random.shuffle(noun)
        pseudonym = adjective[1]+'_'+noun[2]+str(random.randint(100,999))
        cursor.execute("SELECT COUNT(*) FROM sessions WHERE pseudonym=%s", (pseudonym, ))
        count = cursor.fetchall()
    try:
        cursor.execute("INSERT INTO sessions (email, login_time, pseudonym ) VALUES (%s, %s, %s)", (username, datetime.now(), pseudonym))
    except Exception as e:
        print(e)"""
    try:
        cursor.execute("INSERT INTO sessions (email, login_time, pseudonym ) VALUES (%s, %s, %s)", (username, datetime.now(), pseudonym))
    except Exception as e:
        print(e)
    session_id = cursor.lastrowid
    conn.commit()
    conn.close()
    print(1)
    return session_id

def end_session(session_id):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("UPDATE sessions SET logout_time = %s WHERE id = %s", (datetime.now(), session_id))
    conn.commit()
    conn.close()
def get_room_history(room, skip= 0,limit=50):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(
        "SELECT sender, content, timestamp FROM messages WHERE room=%s ORDER BY timestamp DESC LIMIT %s",
        (room, limit)
    )
    rows = cursor.fetchall()
    print(rows)
    conn.close()
    return list(reversed(rows)) 

def create_room(room_name):
    print(f"[DB] Creating room: {room_name}")
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("INSERT IGNORE INTO rooms (name) VALUES (%s)", (room_name,))
    conn.commit()
    conn.close()
def get_sessions(uname):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT pseudonyms from sessions WHERE email = %s", (uname,))
    conn.commit()
    conn.close()


def get_all_rooms():
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT name FROM rooms")
    rooms = [row[0] for row in cursor.fetchall()]
    print(rooms)
    conn.close()
    return rooms


