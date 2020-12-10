/*
 * Copyright © 2019, 2020 Peter Doornbosch
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
package net.luminis.quic.qlog;

import net.luminis.quic.frame.QuicFrame;
import net.luminis.quic.packet.LongHeaderPacket;
import net.luminis.quic.packet.QuicPacket;
import net.luminis.tls.util.ByteUtils;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static javax.json.stream.JsonGenerator.PRETTY_PRINTING;
import static net.luminis.quic.qlog.QLogEvent.Type.PacketReceived;


/**
 * Manages (collects and stores) the qlog file for exactly one quic connection.
 * The log is identified by the original destination connection id.
 */
public class ConnectionQLog {

    private final byte[] cid;
    private Instant startTime;
    private final JsonGenerator jsonGenerator;

    public ConnectionQLog(byte[] cid) throws IOException {
        this.cid = cid;
        this.startTime = Instant.now();
        // Buffering not needed on top of output stream, JsonGenerator has its own buffering.
        OutputStream output = new FileOutputStream(format(cid) + ".qlog");

        boolean prettyPrinting = false;
        Map<String, ?> configuration = prettyPrinting ? Map.of(PRETTY_PRINTING, "whatever") : emptyMap();
        jsonGenerator = Json.createGeneratorFactory(configuration).createGenerator(output);
        writeHeader();
    }

    public void process(QLogEvent event) {
        if (event.getType() == QLogEvent.Type.PacketSent) {
            writePacketEvent(event);
        }
        else if (event.getType() == PacketReceived) {
            writePacketEvent(event);
        }
        else if (event.getType() == QLogEvent.Type.EndConnection) {
            close();
        }
    }

    public void close() {
        writeFooter();
    }

    private void writeHeader() {
        jsonGenerator.writeStartObject()
                .write("qlog_version", "draft-01")
                .writeStartArray("traces")
                .writeStartObject()
                .writeStartObject("vantage_point")
                .write("name", "kwik")
                .write("type", "server")
                .writeEnd()
                .writeStartObject("configuration")
                .write("time_units", "ms")
                .writeEnd()
                .writeStartObject("common_fields")
                .write("ODCID", ByteUtils.bytesToHex(cid))
                .write("reference_time", startTime.toEpochMilli())
                .writeEnd()
                .writeStartArray("event_fields")
                .write("relative_time")
                .write("category")
                .write("event_type")
                .write("data")
                .writeEnd()
                .writeStartArray("events");
    }

    private void writePacketEvent(QLogEvent event) {
        QuicPacket packet = event.getPacket();
        jsonGenerator.writeStartArray()
                .write(Duration.between(startTime, event.getTime()).toMillis())
                .write("transport")
                .write(event.getType() == PacketReceived? "packet_received": "packet_sent")
                .writeStartObject()
                .write("packet_type", formatPacketType(packet))
                .writeStartObject("header")
                .write("packet_number", packet.getPacketNumber())
                .write("packet_size", packet.getSize())
                .write("dcid", format(packet.getDestinationConnectionId()))
                .writeEnd();

        if (packet instanceof LongHeaderPacket) {
            jsonGenerator.write("scid", format(((LongHeaderPacket) packet).getSourceConnectionId()));
        }
        jsonGenerator.writeStartArray("frames");
        packet.getFrames().stream().forEach(frame -> jsonGenerator.writeStartObject().write("frame_type", formatFrame(frame)).writeEnd());
        jsonGenerator.writeEnd()
                .writeEnd()
                .writeEnd();
    }

    private String formatPacketType(QuicPacket packet) {
        if (packet instanceof LongHeaderPacket) {
            return packet.getEncryptionLevel().name().toLowerCase();
        }
        else {
            return "1RTT";
        }
    }

    private String formatFrame(QuicFrame f) {
        return f.getClass().getSimpleName().replace("Frame", "").toLowerCase();
    }

    private String format(byte[] data) {
        return ByteUtils.bytesToHex(data);
    }

    private void writeFooter() {
        jsonGenerator.writeEnd()
                .writeEnd()
                .writeEnd()
                .writeEnd();
        jsonGenerator.close();
        System.out.println("QLog: done with " + format(cid) + ".qlog");
    }

}
