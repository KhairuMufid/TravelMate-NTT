package com.example.travelmatentt.data.response

data class Destination(
    val destinasi_wisata_id: String,
    val nama_objek: String?,
    val jenis_objek: String?,
    val koordinat: String?,
    val alamat: String?,
    val kabupaten_kota: String?,
    val rating: String?,
    val jumlah_reviewer: String?,
    val deskripsi: String?,
    val estimasi_harga_tiket: String?,
    val picture_url: String?
)