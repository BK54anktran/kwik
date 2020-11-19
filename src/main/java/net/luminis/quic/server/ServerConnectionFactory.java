/*
 * Copyright © 2020 Peter Doornbosch
 *
 * This file is part of Kwik, a QUIC client Java library
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic.server;

import net.luminis.quic.Version;
import net.luminis.quic.log.Logger;
import net.luminis.tls.handshake.TlsServerEngineFactory;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.security.SecureRandom;


public class ServerConnectionFactory {

    private final int connectionIdLength;
    private final Logger log;
    private final TlsServerEngineFactory tlsServerEngineFactory;
    private DatagramSocket serverSocket;
    private int initalRtt;
    private SecureRandom randomGenerator;

    public ServerConnectionFactory(int connectionIdLength, DatagramSocket serverSocket, TlsServerEngineFactory tlsServerEngineFactory, int initalRtt, Logger log) {
        if (connectionIdLength > 20 || connectionIdLength < 0) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.2
            // "In QUIC version 1, this value MUST NOT exceed 20 bytes"
            throw new IllegalArgumentException();
        }
        this.tlsServerEngineFactory = tlsServerEngineFactory;
        this.connectionIdLength = connectionIdLength;
        this.log = log;
        this.serverSocket = serverSocket;
        this.initalRtt = initalRtt;

        randomGenerator = new SecureRandom();
    }

    public ServerConnection createNewConnection(Version version, InetSocketAddress clientAddress, byte[] originalScid, byte[] originalDcid) {
        byte[] scid = generateNewConnectionId();
        // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-7.2
        // "A server MUST set the Destination Connection ID it uses for sending packets based on the first received Initial packet."
        byte[] dcid = originalScid;
        return new ServerConnection(version, serverSocket, clientAddress, scid, dcid, originalDcid, tlsServerEngineFactory, initalRtt, log);
    }

    private byte[] generateNewConnectionId() {
        byte[] connectionId = new byte[connectionIdLength];
        randomGenerator.nextBytes(connectionId);
        return connectionId;
    }
}
