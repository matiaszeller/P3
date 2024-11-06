#Simple script that recieves a login request and sends a response based on wether it is true or False

import socket
import sys


def main():
    # Create a TCP/IP socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # Bind the socket to the address given on the command line
    server_address = ('localhost', 10000)
    print('starting up on {} port {}'.format(*server_address))
    sock.bind(server_address)

    # Listen for incoming connections
    sock.listen(1)

    while True:
        # Wait for a connection
        print('waiting for a connection')
        connection, client_address = sock.accept()

        try:
            print('connection from', client_address)

            # Receive the data in small chunks and retransmit it
            while True:
                data = connection.recv(16)
                print('received {!r}'.format(data))
                if data.decode(encoding='UTF-8').strip() == 'test':
                    print('passcode correct, sending data back to the client')
                    connection.sendall(b'correct')
                    break
                elif data.decode(encoding='UTF-8').strip() != 'test':
                    print('sending data back to the client')
                    connection.sendall(b'incorrect')
                    break
                else:
                    print('no data from', client_address)
                    break

        finally:
            # Clean up the connection
            connection.close()
            print('connection closed')
if __name__ == "__main__":
    main()