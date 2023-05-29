package com.kainosdub.rpbank;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncryptDecryptSingleton {
    private static RSAEncryptDecryptSingleton instance = null;
    private static String publicKeyStr = "-----BEGIN PUBLIC KEY-----\n" +
            "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvVWcF72SARmle0b5cKFy\n" +
            "L5HdY7uhj6Txq9hQPprYmh+edUC7BU+0UMyIn0qyU+QglYlNbtIh44Cthwgh7HEi\n" +
            "NckBiHY9jGOBGoC47o09j5W3NSZG6QbhCofKI5tFF6CI15j+/KSf4gfPeHC92nAx\n" +
            "LheaPdCO67BhYoegGSA3XfMCSg8iGtwiLEEo420VOdkccM/mMq8ZentfPSq+8bvq\n" +
            "ixxjwGArbebQbZVhZzY/zLCL9i6eaVB+yVSHa+QCXxhXKRAHGs3w/M8Yb0vDvuFG\n" +
            "/6a+81sxUAfOKMd2/tI/R1jxyXO7j4GBSjXrfWdA3SeYsdK58ieA4pj+bdBcIYrM\n" +
            "XknxbidHEWQbNCTrnVdnauXL5f3qrD0KCyt3Bn4cYrq/tQfjK7SseGxHbrvTAssE\n" +
            "kdeBmKOHe4aTCIUDAqDmADx4yEywfBR3rV1CGsu5+kkutswb+hOh2AajdNHkOz6s\n" +
            "g7+jo/kkBNgkJ+oTRWPwPri5PqeCgHJg+mdaHQK4aiuNGTt1cWgkfcb4p8Ye5+0y\n" +
            "QPJkYQQvkEwAwnMjA0PGQAd7foUuOkro+oui1IYgR97T+nO2F2i5JIgDsmfURQj2\n" +
            "BjMB2fW3z6/VVXp8PyWIna4dZEdRI5qWKcyZzLmJZxBAAtG4R0C+V8kUGxAXmtLz\n" +
            "IzGz76FZlsSOFEDPUB4z9YUCAwEAAQ==\n" +
            "-----END PUBLIC KEY-----\n";
    private static String privateKeyStr = "-----BEGIN PRIVATE KEY-----\n" +
            "MIIJQwIBADANBgkqhkiG9w0BAQEFAASCCS0wggkpAgEAAoICAQC9VZwXvZIBGaV7\n" +
            "RvlwoXIvkd1ju6GPpPGr2FA+mtiaH551QLsFT7RQzIifSrJT5CCViU1u0iHjgK2H\n" +
            "CCHscSI1yQGIdj2MY4EagLjujT2Plbc1JkbpBuEKh8ojm0UXoIjXmP78pJ/iB894\n" +
            "cL3acDEuF5o90I7rsGFih6AZIDdd8wJKDyIa3CIsQSjjbRU52Rxwz+Yyrxl6e189\n" +
            "Kr7xu+qLHGPAYCtt5tBtlWFnNj/MsIv2Lp5pUH7JVIdr5AJfGFcpEAcazfD8zxhv\n" +
            "S8O+4Ub/pr7zWzFQB84ox3b+0j9HWPHJc7uPgYFKNet9Z0DdJ5ix0rnyJ4DimP5t\n" +
            "0FwhisxeSfFuJ0cRZBs0JOudV2dq5cvl/eqsPQoLK3cGfhxiur+1B+MrtKx4bEdu\n" +
            "u9MCywSR14GYo4d7hpMIhQMCoOYAPHjITLB8FHetXUIay7n6SS62zBv6E6HYBqN0\n" +
            "0eQ7PqyDv6Oj+SQE2CQn6hNFY/A+uLk+p4KAcmD6Z1odArhqK40ZO3VxaCR9xvin\n" +
            "xh7n7TJA8mRhBC+QTADCcyMDQ8ZAB3t+hS46Suj6i6LUhiBH3tP6c7YXaLkkiAOy\n" +
            "Z9RFCPYGMwHZ9bfPr9VVenw/JYidrh1kR1EjmpYpzJnMuYlnEEAC0bhHQL5XyRQb\n" +
            "EBea0vMjMbPvoVmWxI4UQM9QHjP1hQIDAQABAoICAAc9AiIwN6g+HEL5xCiHq49h\n" +
            "ArdA4ZzVv/2DYBH8poJB6jNuXZgG44xhPWnll6q4YnyFCsZNV0lUzo2GhJF/A8FN\n" +
            "pXbbml/HIBTszeUk1jEqlp38ECLxheH6rgItefc8xm6DpV/wRUKFbOucV83Fk0PB\n" +
            "WD67vfMJw7daGwdK4YMAetps+K9RMidB+He1YGXdRIaVlCXk5tL1a38xpqokNoPJ\n" +
            "+pBMvOxPMjG2T8p72vWO3FL1lk3Na4Nz7Vd1GJgdHJvvxm3CaM+pdTQwD9Q41ZeJ\n" +
            "fuxb1KdMHRgXBBga3ptyLZA3kfibCV/WbuHU9DhgPqixtzUoSHehRLzbBeKsZUK2\n" +
            "sKwX4wy2SFyhyoFvQNHHrN8bUd1Vcfe87g0pMsFMDUOY48WfNE6mk4GB1P4AzM2R\n" +
            "RWhoyX7irj/gsIPa4+0+wYt/jRk+z4tVCCkvSICucLzX8J3PBOJO6QL2KnYRHzy0\n" +
            "HoyboxtuEUI1WXxYfLHgHyfrbv2VkAynGsOmDdVLQl5nbhl2+6BxVT9k9Xm+yHjI\n" +
            "M3kRU8Uv4QwNsi5hwlhUtAS2HywHJurcMk1nIR8QbQYsSJ+nNmNcw2gSnp+DygJV\n" +
            "j+JL/V+WwYvW3uhOenODqbXstGqI3dAueRN+pnWjkCuVzQSigkXCqYMWC0Onm+VH\n" +
            "z/eo8F9h6cvKEzVzJLahAoIBAQDPPb/46Xam6nyLn2DYtV/ltyXw7Mki5uyYi8FZ\n" +
            "88+mJlXVwVo9jCqm7N4IjiwS37gZTzFrh2SKQTsdFcND0+qnBud2JVIfsHPJKIxn\n" +
            "GnK3lGbvMzAeucJB0adhbmpihUpn/EpGD+rqQBIQEcSp7n76UHBGbh7CvE7asGf7\n" +
            "u1pQCQ1gzh0v/cSdYw4juegrUHMurTopWJtgUYhTHJTbDtsmiGThJvWhSl6YXq1B\n" +
            "tvBir1MAb206BEjyXwZu7OC9XY5aZOW+WQ3Ib9aIxmRsGWRRlEMKjjU8HaDGcO3/\n" +
            "dQgwV36RaAgZipuZAwEv7MhNdfjg6q3MMEK010vl7715N60VAoIBAQDp4VLhqyU0\n" +
            "HV0bZ2RRGXDemmrrU/Q8Ytuo22x0BFqY6OZotQAcWcrLUKVOHZG4n6Crp5RpT0II\n" +
            "0nxwNsIOKZ8x8pfS9SPv5TLg1YlXUjzVWmxnGvuMg9FVG/6rbFdDUbITu4G7sLqi\n" +
            "DsQz2WEOF8gkEg4gncEcwLH4hHA7GYiEV/36lUE00jtjjKL2WEHjrgudK1k/ZYpw\n" +
            "VceP1Ph0MBrZC+LpXb6GRcQ/KnI4QAsNlKDHB4blzNx9F/5FkP0ojAqP/s0YmRdp\n" +
            "TGbxBWEy1+dAhER/FuUKzg0OaNXDbutc/X+a7q0KMZztx0EN2tmVoKGijVWP4iFn\n" +
            "g+4FC9z99qKxAoIBAQCXT7Scnoj9MfOhVcq2LydHZ8OR9rCchRJ2BoQzkyonW5IM\n" +
            "MdIbYf26RvOON4/CcAnQoNuqcP5dW2c3wy4Alfeb4BSbVIBzlrfTRYHNvafIldfa\n" +
            "Cfu2U1acC+Ez6BRQvpUm+zOXmAOi6QjHJtH6aKHZTWXMZpabBDZmwaoKSC6WhSV5\n" +
            "asQwyA4IA8zNFO2IwoJ2sA/pJEK9vonUdOfSUTR9G9Tb90AcdVo/0dCaTGGTDAOE\n" +
            "K9cKJxrDq9Hcp6MnX+mR4l1D7216zP1Me93Sd2+hiKiySkZgEBnVCZsbi21hLmDA\n" +
            "9b4EOAmHXIQ/Y5iTxfDi9zXSAeKSeyd1SOeEW7xJAoIBAQCZuk5lIL5qe+aILbSF\n" +
            "jghfePZQSjWeP4iMe/XUaEw4d9WC+33gJLEkZJTTPKJczSepzJPDiKIp9Fhw1b1F\n" +
            "29vU09Uxh4ogk/GWUSVeLSLpRe888kJnwPkmTSle+e59xEQdrkD+4pI6FSSnw/mE\n" +
            "buNRukBo9ehKAuq4JC0023qdKs05GUPr+UeqDnXLIIXmpq7hlu2puw98+RUcGGta\n" +
            "y4fKJIL4y3KBBXiR4E+FY6sgORJY4Dyt7bL70nqCtWOBdFM5BM1AntgBkYOUZunV\n" +
            "po5NHON8+cqBCKESWJwxQkYYMFPgvYMl0SiKLk83USN1s0iq9OVJluRICzK3RG30\n" +
            "zFlxAoIBAERhQX7Lvout76XDh6Ibny5HblQC3OPwR0lYTG35Sy4iEUBpx9G2oRgl\n" +
            "Xb6J1xGjAQFZ4lGVumV6+/ImHhSloVYHTJzpxSLohQBg+M+NOfY6F1Y9ftM7cUqv\n" +
            "ozzLvQlExHHlGSaY1XRZBPfSu3QqE5U6Snf7vuhznhkbGofmIeVFImZKXE3SlSiX\n" +
            "MPIx6CemdRh3fkM/mv0T83As0QCoFa6Q+zLqPKQ0fnapH3V2Cq1p4kC/MIpJ1oVQ\n" +
            "hYHuJxZBqV+WKucmKX91hs3b+u3n58KcXn1Bhn/ebr7H1047SDDKxC7nPTGG3d+G\n" +
            "7Rs82r9SFC1sDAxxdyWKrDcijLi1SVw=\n" +
            "-----END PRIVATE KEY-----\n";
    private PublicKey publicKey = null;
    private PrivateKey privateKey = null;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public RSAEncryptDecryptSingleton(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public static RSAEncryptDecryptSingleton getInstance() throws Exception {
        if (instance == null) {
            PublicKey pubKey = readPublicKeyFromString(publicKeyStr);
            PrivateKey priKey = readPrivateKeyFromString(privateKeyStr);
            instance = new RSAEncryptDecryptSingleton(pubKey, priKey);
        }
        return instance;
    }

    private static PublicKey readPublicKeyFromString(String publicKeyStr) throws Exception {
        publicKeyStr = publicKeyStr.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", ""); // remove extra spaces

        byte[] decoded = Base64.getDecoder().decode(publicKeyStr);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private static PrivateKey readPrivateKeyFromString(String privateKeyStr) throws Exception {
        privateKeyStr = privateKeyStr.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""); // remove extra spaces

        byte[] decoded = Base64.getDecoder().decode(privateKeyStr);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private String encryptWithPublicKey(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decryptWithPrivateKey(PrivateKey privateKey, String encryptedBase64) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public String encrypt(String message) throws Exception {
        return encryptWithPublicKey(publicKey, message);
    }

    public String decrypt(String message) throws Exception {
        return decryptWithPrivateKey(privateKey, message);
    }
}

