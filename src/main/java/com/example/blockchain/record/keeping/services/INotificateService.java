package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Notifications;

import java.util.List;

public interface INotificateService {

    Notifications save(Notifications notifications);

    List<Notifications> saveAll(List<Notifications> list);

}
