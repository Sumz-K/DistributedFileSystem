class DummyClass {
   DummyClass() {
   }

   static String bytesToHex(byte[] var0) {
      StringBuilder var1 = new StringBuilder();
      byte[] var2 = var0;
      int var3 = var0.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         byte var5 = var2[var4];
         var1.append(String.format("%02x", var5));
      }

      return var1.toString();
   }
}