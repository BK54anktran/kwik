/*
 * Copyright © 2023 Peter Doornbosch
 *
 * This file is part of Kwik, an implementation of the QUIC protocol in Java.
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
package net.luminis.quic.packet;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import net.luminis.quic.core.Version;

class LongHeaderPacketTest {

    @Test
    void testDetermineTypeV1() {
        assertThat(LongHeaderPacket.determineType((byte) 0b1100_0000, Version.QUIC_version_1)).isEqualTo(InitialPacket.class);
        assertThat(LongHeaderPacket.determineType((byte) 0b1101_0000, Version.QUIC_version_1)).isEqualTo(ZeroRttPacket.class);
        assertThat(LongHeaderPacket.determineType((byte) 0b1110_0000, Version.QUIC_version_1)).isEqualTo(HandshakePacket.class);
        assertThat(LongHeaderPacket.determineType((byte) 0b1111_0000, Version.QUIC_version_1)).isEqualTo(RetryPacket.class);
    }

    @Test
    void testDetermineTypeV2() {
        assertThat(LongHeaderPacket.determineType((byte) 0b1101_0000, Version.QUIC_version_2)).isEqualTo(InitialPacket.class);
        assertThat(LongHeaderPacket.determineType((byte) 0b1110_0000, Version.QUIC_version_2)).isEqualTo(ZeroRttPacket.class);
        assertThat(LongHeaderPacket.determineType((byte) 0b1111_0000, Version.QUIC_version_2)).isEqualTo(HandshakePacket.class);
        assertThat(LongHeaderPacket.determineType((byte) 0b1100_0000, Version.QUIC_version_2)).isEqualTo(RetryPacket.class);
    }
}