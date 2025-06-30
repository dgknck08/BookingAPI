# BookingAPI

BookingAPI, otel veya benzeri rezervasyon sistemleri için geliştirilmiş RESTful bir API'dir. Java Spring Boot ile yapılmıştır ve performans için Redis cache entegrasyonu içerir.

---

## Özellikler

- Kullanıcı kayıt ve giriş işlemleri (Session-based Authentication)
- Rezervasyon CRUD işlemleri
- Redis cache ile sık erişilen verilerin hızlı sunulması
- Redis ile session yönetimi
- PostgreSQL veya MySQL gibi ilişkisel veritabanı desteği
- Basit ve anlaşılır REST API endpointleri

---

## Teknolojiler

- Java 17
- Spring Boot
- Spring Security (Session-based Authentication)
- Redis (Cache ve Session yönetimi için)
- JPA / Hibernate
- Maven

---

## Redis Kullanımı

BookingAPI performansı artırmak amacıyla Redis kullanır. Özellikle:

- Sık sorgulanan rezervasyon verileri Redis cache'de tutulur, böylece veritabanı yükü azalır ve API daha hızlı yanıt verir.
- Kullanıcı session bilgileri Redis üzerinde tutulur. Bu, dağıtık ortamda session tutmayı kolaylaştırır ve ölçeklenebilirliği artırır.

Redis bağlantısı `application.properties` dosyasında yapılandırılmıştır ve Spring RedisTemplate ile cache işlemleri yapılmaktadır.

---