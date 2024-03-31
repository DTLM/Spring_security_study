package com.thiago.Spring_security_study.service;

import com.thiago.Spring_security_study.modal.Role;

public interface IRoleService {

	Role getByNome(String nome);
}
