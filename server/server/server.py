import asyncio
import hashlib
from datetime import datetime
import websockets
from db import (
    create_user, verify_user, log_message, start_session, end_session,
    get_room_history, create_room, get_all_rooms, get_sessions
)

HOST = '0.0.0.0'
PORT = 5000
clients = {}  # websocket: {"username": str, "room": str, "session": id}
rooms = {}    # room_name: set of websockets
async def send(ws, text):
    await ws.send(text)

async def broadcast(room, message, sender_ws=None):
    for client in rooms.get(room, set()):
        if client != sender_ws:
            try:
                await client.send(message)
            except:
                pass

async def handle_client(ws):
    await send(ws, "Welcome! Please /login <user> <pass> or /register <user> <pass>")
    clients[ws] = {"username": None, "room": None, "session": None}
    try:
        async for msg in ws:
            msg = msg.strip()
            print(msg)
            print(clients[ws])
            user = clients[ws]
            username = user["username"]
            room = user["room"]
            if msg.startswith("/register"):
                parts = msg.split()
                if len(parts) != 2:
                    await send(ws, "Usage: /register <username>")
                    continue
                _, uname = parts
                try:
                    result = create_user(uname)
                    await send(ws, f"{result}")
                    clients[ws]["session"] = start_session(uname)
                    
                except Exception as e:
                    await send(ws, f"Registration failed: {e}")

            elif msg.startswith("/login"):
                parts = msg.split()
                if len(parts) != 2:
                    await send(ws, "Usage: /login <username>")
                    continue
                _, uname = parts
                if verify_user(uname):
                    clients[ws]["username"] = uname
                    clients[ws]["session"] = start_session(uname)
                    await send(ws, f"Welcome {uname}")
                else:
                    await send(ws, "Login failed")

            elif username:
                if msg.startswith("/create"):
                    parts = msg.split()
                    if len(parts) != 2:
                        await send(ws, "Usage: /create <room>")
                        continue
                    _, room_name = parts
                    if room_name in rooms:
                        await send(ws, f"Room '{room_name}' already exists")
                    else:
                        create_room(room_name)
                        rooms[room_name] = set()
                        await send(ws, f"Room '{room_name}' created")

                elif msg.startswith("/join"):
                    parts = msg.split()
                    if len(parts) != 2:
                        await send(ws, "Usage: /join <room>")
                        continue
                    _, room_name = parts
                    if room_name not in rooms:
                        await send(ws, "Room doesn't exist")
                    else:
                        if room:
                            rooms[room].discard(ws)
                        rooms[room_name].add(ws)
                        clients[ws]["room"] = room_name
                        await send(ws, f"Joined room '{room_name}'")
                        await broadcast(room_name, f"{username} joined the room", ws)

                elif msg.startswith("/leave"):
                    if room:
                        rooms[room].discard(ws)
                        await broadcast(room, f"{username} left the room", ws)
                        clients[ws]["room"] = None
                        await send(ws, "You left the room")
                    else:
                        await send(ws, "You're not in a room")

                elif msg.startswith("/list"):
                    print("Rooms")
                    room_list = ", ".join(rooms.keys()) or "No active rooms"
                    await send(ws, f"Rooms: {room_list}")

                elif msg.startswith("/whoami"):
                    await send(ws, f"You are {username}")

                elif msg.startswith("/active"):
                    if room:
                        users = [clients[c]["username"] for c in rooms[room]]
                        await send(ws, f"Users in '{room}': {', '.join(users)}")
                    else:
                        await send(ws, "You're not in a room")

                elif msg.startswith("/history"):
                    if room:
                        history = get_room_history(room)
                        if history:
                            await send(ws, "Last messages:")
                            for sender, content, ts in history:
                                line = f"[{ts.strftime('%H:%M')}] {sender}: {content}"
                                await send(ws, line)
                        else:
                            await send(ws, "No message history")
                    else:
                        await send(ws, "You're not in a room")

                

                else:
                    if room:
                        log_message(username, room, msg)
                        await broadcast(room, f"{username}: {msg}", ws)
                    else:
                        await send(ws, "Join a room to chat")

            else:
                await send(ws, "Please /login first")

    except websockets.ConnectionClosed:
        print(f"{username or 'Unknown'} disconnected")
    finally:
        if ws in clients:
            room = clients[ws]["room"]
            if room and ws in rooms.get(room, []):
                rooms[room].discard(ws)
                await broadcast(room, f"{username} left", ws)
            if clients[ws]["session"]:
                end_session(clients[ws]["session"])
            del clients[ws]
def load_rooms():
    global rooms
    rooms = {}
    try:
        room_names = get_all_rooms()
        for room in room_names:
            rooms[room] = set()
        print(f"Loaded rooms: {list(rooms.keys())}")
    except Exception as e:
        print(f"Error loading rooms from DB: {e}")
async def main():
    load_rooms()
    print(f"WebSocket Chat Server running on ws://{HOST}:{PORT}")
    async with websockets.serve(handle_client, HOST, PORT) as server:
        await asyncio.Future() 
if __name__ == "__main__":
    asyncio.run(main())
