package com.tomatoes.web.Seguridad;

import com.tomatoes.web.Excepciones.ApiException;
import com.tomatoes.web.Excepciones.ServicioNoDisponibleException;
import com.tomatoes.web.Servicio.ClienteApi;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Autentica contra ms-empleados (POST /api/auth/login): el frontend no guarda contraseñas ni usuarios. */
@Component
public class ProveedorAutenticacion implements AuthenticationProvider {

    private final ClienteApi api;

    public ProveedorAutenticacion(ClienteApi api) {
        this.api = api;
    }

    @Override
    public Authentication authenticate(Authentication autenticacion) throws AuthenticationException {
        String correo = autenticacion.getName();
        String clave = String.valueOf(autenticacion.getCredentials());
        Map<String, Object> r;
        try {
            r = api.login(correo, clave);
        } catch (ApiException e) {
            throw new BadCredentialsException(e.getMessage());
        } catch (ServicioNoDisponibleException e) {
            throw new AuthenticationServiceException(e.getMessage());
        }
        UsuarioSesion usuario = new UsuarioSesion(texto(r.get("id")), texto(r.get("correo")), texto(r.get("nombre")),
                texto(r.get("rol")), texto(r.get("token")));
        return new UsernamePasswordAuthenticationToken(usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol())));
    }

    @Override
    public boolean supports(Class<?> tipo) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(tipo);
    }

    private static String texto(Object o) {
        return o == null ? "" : o.toString();
    }
}
