import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class hash_and_file {
    byte[] file;
    HashMap<String, byte[]> map = new HashMap<>();

    hash_and_file(byte[] file) {
        this.file = file;
    }

    public Map<String, byte[]> split_and_give_me() throws IOException {
        FileSplitting splitter = new FileSplitting();
        List<byte[]> chunks = splitter.splitBysize(file, 128000);
        String[] hashes = splitter.getHash(chunks);
        for (int i = 0; i < hashes.length; i++) {
            map.put(hashes[i], chunks.get(i));
        }
        return map;
    }
}
