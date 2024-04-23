import java.io.*;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class FileSplitting {
    String algorithm = "SHA-256";

    public List<byte[]> splitBysize(byte[] x, int chunkSize) throws IOException {
        List<byte[]> chunks = new ArrayList<>();
        int offset = 0;

        while (offset < x.length) {
            int length = Math.min(chunkSize, x.length - offset);
            byte[] chunk = new byte[length];
            System.arraycopy(x, offset, chunk, 0, length);
            chunks.add(chunk);
            offset += length;
        }

        return chunks;
    }

    public String[] getHash(List<byte[]> array) {
        ArrayList<String> list = new ArrayList<>();
        for (byte[] i : array) {
            try {
                byte[] hash = hashByteArray(i, algorithm);
                list.add(DummyClass.bytesToHex(hash));
            } catch (NoSuchAlgorithmException e) {
                System.out.println("error in getting hash");
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String getFilehash(byte[] bytearray) {
        String ret = "";
        try {
            byte[] hash = hashByteArray(bytearray,algorithm);
            ret = DummyClass.bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("error in getting hash");
        }

        return ret;
    }

    private byte[] hashByteArray(byte[] array, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return digest.digest(array);
    }

    public byte[] join(List<byte[]> list) {
        int totalLength = list.stream().mapToInt(arr -> arr.length).sum();
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] arr : list) {
            System.arraycopy(arr, 0, result, offset, arr.length);
            offset += arr.length;
        }
        return result;
    }
}