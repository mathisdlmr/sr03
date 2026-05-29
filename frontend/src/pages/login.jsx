import { useEffect, useState, useRef } from 'react'

import "@olton/metroui/lib/metro.css";
import "@olton/metroui/lib/icons.css";
import "@olton/metroui/lib/metro.js";

function LoginScreen() {
  return (
    <div class="container mt-20">
        <div class="row">
            <div class="cell-md-6 offset-md-3">
                <div class="border border-size-1 border-radius-10 bd-blue p-8">
                    <div class="text-center mb-6">
                        <h2 class="text-bold mt-2">Panel Admin</h2>
                        <p class="text-muted">Connectez-vous pour accéder au panel admin</p>
                    </div>
                    <div class="alert border-radius-2 alert-warning mb-4">
                        <span class="mif-warning mx-2"></span>
                        <span>Erreur</span>
                    </div>

                    <form method="post">
                        <div class="form-group">
                            <label class="label-for-input">Adresse e-mail</label>
                            <input type="email" name="mail" data-role="input" placeholder="john.doe@mail.fr" data-prepend="&lt;span class='mif-person'&gt;" required/>
                        </div>
                        <div class="form-group mt-4">
                            <label class="label-for-input">Mot de passe</label>
                            <input type="password" name="password" data-role="input" placeholder="*********" data-prepend="<span class='mif-lock'></span>" data-reveal-button-icon="<span class='mif-eye mif-2x'></span>" required/>
                        </div>
                        <div class="form-group mt-6">
                            <button type="submit" class="button info bg-blue fg-white text-bold w-100">
                                <span class="mif-enter mr-2"></span>
                                Se connecter
                            </button>
                        </div>
                    </form>
                    <div class="mt-4 text-center">
                        <a>Mot de passe oublié ?</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
  );
}

export default LoginScreen;
