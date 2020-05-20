/*
 * Copyright 2018 Coinomi Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.horizontalsystems.binancechainkit.helpers;


import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.horizontalsystems.hdwalletkit.ECDSASignature;
import io.horizontalsystems.hdwalletkit.ECException;
import io.horizontalsystems.hdwalletkit.ECKey;

import static com.google.common.base.Preconditions.checkArgument;
import static io.horizontalsystems.hdwalletkit.ECKey.HALF_CURVE_ORDER;


public class Crypto {

    public static byte[] sign(byte[] msg, BigInteger privKey) throws NoSuchAlgorithmException, ECException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] msgHash = digest.digest(msg);

        ECDSASignature signature = doSign(msgHash, privKey);

        byte[] result = new byte[64];
        System.arraycopy(bigIntegerToBytes(signature.getR(), 32), 0, result, 0, 32);
        System.arraycopy(bigIntegerToBytes(signature.getS(), 32), 0, result, 32, 32);

        return result;

    }

    public static byte[] decodeAddress(String address) throws SegwitAddressException {

        byte[] dec = Bech32.decode(address).getData();
        return convertBits(dec, 0, dec.length, 5, 8, false);
    }

    public static String encodeAddress(String hrp, byte[] code) {
        byte[] convertedCode = Crypto.convertBits(code, 0, code.length, 8, 5, true);
        return Bech32.encode(hrp, convertedCode);
    }

    /**
     * see https://github.com/sipa/bech32/pull/40/files
     */
    public static byte[] convertBits(final byte[] in, final int inStart, final int inLen,
                                     final int fromBits, final int toBits, final boolean pad)
            throws SegwitAddressException {
        int acc = 0;
        int bits = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        final int maxv = (1 << toBits) - 1;
        final int max_acc = (1 << (fromBits + toBits - 1)) - 1;
        for (int i = 0; i < inLen; i++) {
            int value = in[i + inStart] & 0xff;
            if ((value >>> fromBits) != 0) {
                throw new SegwitAddressException(String.format(
                        "Input value '%X' exceeds '%d' bit size", value, fromBits));
            }
            acc = ((acc << fromBits) | value) & max_acc;
            bits += fromBits;
            while (bits >= toBits) {
                bits -= toBits;
                out.write((acc >>> bits) & maxv);
            }
        }
        if (pad) {
            if (bits > 0) out.write((acc << (toBits - bits)) & maxv);
        } else if (bits >= fromBits || ((acc << (toBits - bits)) & maxv) != 0) {
            throw new SegwitAddressException("Could not convert bits, invalid padding");
        }
        return out.toByteArray();
    }


    public static class SegwitAddressException extends IllegalArgumentException {

        SegwitAddressException(String s) {
            super(s);
        }
    }

    public static byte[] bigIntegerToBytes(BigInteger b, int numBytes) {
        checkArgument(b.signum() >= 0, "b must be positive or zero");
        checkArgument(numBytes > 0, "numBytes must be positive");
        byte[] src = b.toByteArray();
        byte[] dest = new byte[numBytes];
        boolean isFirstByteOnlyForSign = src[0] == 0;
        int length = isFirstByteOnlyForSign ? src.length - 1 : src.length;
        checkArgument(length <= numBytes, "The given number does not fit in " + numBytes);
        int srcPos = isFirstByteOnlyForSign ? 1 : 0;
        int destPos = numBytes - length;
        System.arraycopy(src, srcPos, dest, destPos, length);
        return dest;
    }

    private static boolean isCanonical(ECDSASignature ecSig) {
        return ecSig.getS().compareTo(HALF_CURVE_ORDER) <= 0;
    }

    private static ECDSASignature toCanonicalised(ECDSASignature ecSig) {

        if (!isCanonical(ecSig)) {
            // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
            // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
            //    N = 10
            //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
            //    10 - 8 == 2, giving us always the latter solution, which is canonical.
            return new ECDSASignature(ecSig.getR(), ECKey.ecParams.getN().subtract(ecSig.getS()));
        } else {
            return ecSig;
        }
    }


    private static ECDSASignature doSign(byte[] input, BigInteger privateKeyForSigning) throws ECException {

        try {

            ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
            ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, ECKey.ecParams);
            signer.init(true, privKey);
            BigInteger[] components = signer.generateSignature(input);

            return toCanonicalised(new ECDSASignature(components[0], components[1]));
        } catch (RuntimeException exc) {

            throw new ECException("Exception while creating signature", exc);
        }
    }

}