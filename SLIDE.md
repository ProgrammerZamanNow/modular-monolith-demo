---
theme: default
class: text-center
---

# Modular Monolith Architecture
### The Pragmatic Path to Microservices
**Studi Kasus: E-Commerce Demo Project**

---
layout: default
---

# Masalah pada Monolith Tradisional
Mengapa kita seringkali membenci aplikasi Monolith seiring berjalannya waktu?

- **Spaghetti Code**: Tidak ada batasan akses antar domain. Logic *Order* bisa langsung mengubah tabel *User*.
- **High Coupling**: Perubahan pada satu fitur seringkali merusak fitur lain secara tidak terduga.
- **Sulit di-Scale**: Sangat sulit memisahkan satu fitur yang sibuk menjadi layanan independen.
- **Database Tightly Coupled**: Relasi tabel (`JOIN`, `Foreign Key`) saling tumpang tindih sehingga *database migration* sangat menakutkan.

---
layout: default
---

# "Microservices" Bukan *Silver Bullet*
Mengapa langsung lompat ke Microservices sejak Hari ke-1 bisa menjadi bencana?

- **Kompleksitas Infrastruktur**: Membutuhkan Kubernetes, Service Discovery, API Gateway, dsb.
- **Network Overhead**: Komunikasi antar service via HTTP/gRPC jauh lebih lambat dari *method call*.
- **Distributed Transactions**: Kegagalan di tengah transaksi membutuhkan penanganan rumit (Saga Pattern).
- **Over-Engineering**: Seringkali tim menghabiskan waktu mengurus server, bukan fitur bisnis.

---
layout: default
---

# Solusi: Modular Monolith
**Mendapatkan kebebasan desain Microservices, dengan kemudahan deployment Monolith.**

- **Satu Deployment**: Tetap di-deploy sebagai satu kesatuan aplikasi (satu file `.jar`).
- **Logical Boundaries**: Kode dipisah tegas berdasarkan domain (Modul Maven).
- **Encapsulation**: Domain bisnis menyembunyikan logika dan *database*-nya dari domain lain.
- **Future-Proof**: Sangat siap dipecah menjadi Microservices saat bisnis memang sudah membutuhkannya.

---
layout: default
---

# Strategi Pemisahan Modul (The Split)
Untuk memastikan *Strict Boundary*, setiap Domain Bisnis dipecah menjadi **2 Modul Maven**:

1. **Modul Client (Interface & DTO)**
   - Contoh: `ecommerce-order-client`
   - Sangat ringan. Hanya berisi *Interface* dan `record`/POJO.
   - Modul inilah yang **boleh** di-import oleh modul domain lain.

2. **Modul Implementasi (Logic & DB)**
   - Contoh: `ecommerce-order`
   - Berisi Service, Controller, Entity JPA, dan Repository.
   - **Tersembunyi** (hidden) dan tidak boleh di-import oleh modul domain lain.

---
layout: default
---

# Implementasi pada Demo Project
Bagaimana E-Commerce ini dibagi menjadi berbagai domain?

- **`ecommerce-customer`**: Mengelola profil data pelanggan.
- **`ecommerce-product`**: Katalog barang dan manajemen pemotongan/pengembalian stok.
- **`ecommerce-order`**: Sang Orkestrator. Menggabungkan pelanggan, stok, tagihan, dan notifikasi.
- **`ecommerce-payment`**: Mengelola status tagihan dan pembayaran.
- **`ecommerce-notification`**: Pengiriman notifikasi berbasis *Event-Driven*.
- **`ecommerce-app`**: Modul *Aggregator*. Menyuntikkan semua implementasi saat *runtime*.

---
layout: default
---

# Strategi Integrasi Antar-Modul (Loose Coupling)
Bagaimana `Order` bisa mengurangi stok di `Product` tanpa saling mengenal (*coupled*)?

- **Synchronous Call**: Modul `Order` hanya meng-import antarmuka `ecommerce-product-client`. Saat aplikasi berjalan, Spring `ecommerce-app` menyuntikkan `ProductClientImpl`. Ini hanya berupa *method call* standar JVM (sangat cepat).
- **Asynchronous / Event-Driven**: Modul `Notification` memisahkan *sender* dan *processor* menggunakan Spring `ApplicationEventPublisher`. Klien menembakkan *event*, servis mendengarkan (`@EventListener`) tanpa memblokir proses utama.

---
layout: default
---

# Strategi Database Lintas Modul
Bebas dari "Penjara" *Foreign Key*.

- **Tidak Ada `@ManyToOne` Lintas Modul**: Relasi antar tabel modul hanya disimpan sebagai tipe primitif (seperti `String customerId`, `String productId`).
- **Data Integrity via Logic**: Integritas data diverifikasi lewat panggilan API internal (misal `CustomerClient.getCustomer(id)`).
- **Microservice-Ready**: Jika suatu hari database harus dipisah ke beda server fisik, tidak akan ada *Constraint Violation* (error Foreign Key constraint).

---
layout: default
---

# Evolusi ke Microservices: *The Magic Trick*
Saat trafik membludak dan modul `Product` harus dipisah ke server lain, bagaimana caranya?

1. **Kode di `Order` TIDAK DIUBAH**: Kode `productClient.reduceStock()` di `OrderService` sama sekali tidak tersentuh.
2. **Ganti Implementasi Client**: Anda hanya perlu mengganti kelas `ProductClientImpl` menjadi `ProductClientRestImpl` yang isinya memanggil *REST API* menggunakan `RestTemplate` atau `FeignClient`.
3. **Pindahkan Modul Product**: Deploy modul `Product` ke server/container barunya.
4. **Selesai!** Proses migrasi hanya memakan hitungan hari, bukan bulan.

---
layout: default
---

# Kesimpulan
Modular Monolith adalah batu pijakan sempurna untuk sebagian besar *startup* maupun fitur baru.

- **Fokus pada Bisnis**: Tidak pusing infrastruktur di awal.
- **Strict Boundary**: Kode tetap bersih dan rapi layaknya Microservices.
- **Jaring Pengaman (Integration Test)**: Terlindungi penuh oleh *end-to-end API test*.
- **Jalan Keluar Jelas**: Perpindahan ke arsitektur *Distributed System* (Microservices) sudah dipetakan dengan gamblang.

---
class: text-center
---

# Q&A
**Terima Kasih!**
