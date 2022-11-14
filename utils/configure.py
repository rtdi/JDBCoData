#!/usr/bin/env python3
""" Vult configuratie-templates met waarden uit key vault.
Autorisatie op key vault werkt met managed identities.

[Managed identities](https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview)
[Code voorbeeld](https://docs.microsoft.com/en-us/azure/python/python-sdk-azure-authenticate#mgmt-auth-msi)
[Code voorbeeld GitHub](https://github.com/Azure-Samples/resource-manager-python-manage-resources-with-msi)
[Container met MI voorbeeld](https://docs.microsoft.com/en-us/azure/container-instances/container-instances-managed-identity)
[Python template documentatie](https://docs.python.org/3.7/library/string.html#string.Template)
[Azure key vault python client](https://pypi.org/project/azure-keyvault-secrets/): deze is leidend voor implementatie.
"""

from string import Template
from glob import glob
import os
import argparse
import logging
from azure.identity import DefaultAzureCredential
from azure.keyvault.secrets import SecretClient
import pathlib
from typing import List, Dict

logging.basicConfig(level="WARNING")
logger = logging.getLogger(__name__)


class AzTemplate(Template):
    idpattern = '([_a-z][-_a-z0-9]*)'

    @property
    def variables(self) -> List[str]:
        results = [x[self.pattern.groupindex['named']] or x[self.pattern.groupindex['braced']]
                   for x in self.pattern.findall(self.template)]
        return results

    def find_missing_variables(self, mapping: Dict[str, str]) -> list:
        return [v for v in self.variables if v not in mapping]


class EnvDefault(argparse.Action):
    """Store value with given environment variable as default"""

    def __init__(self, envvar, required=True, default=None, **kwargs):
        if envvar and envvar in os.environ:
            default = os.environ[envvar]
        if required and default:
            required = False
        super(EnvDefault, self).__init__(
            default=default, required=required, **kwargs)

    def __call__(self, parser, namespace, values, option_string=None):
        setattr(namespace, self.dest, values)


def get_templates(rootpath="."):
    return glob(os.path.join(rootpath, "**/*.template"), recursive=True)


def generate_config_files(templates: List[str], value_map: Dict[str, str]):
    """Genereer een configuratiebestand voor iedere template.
    Naam van configuratiebestand is de naam van de template zonder de '.template' extentie.
    Variabelen in de 'value_map' worden gebruikt voor de substitution.
    Lijst met configuratiebestanden wordt teruggegeven.
    """

    config_files = []
    translate_table = str.maketrans({"#": "\\#"})
    has_errors = False
    for template in templates:
        # vind alle templates
        with open(template) as fp:
            t_input = AzTemplate(fp.read())

        # Vervang de placeholders en schijf weg zonder template extentie

        output_filename = pathlib.Path(template).with_suffix("")
        if output_filename.suffix == ".properties":
            local_value_map = {k: str(v).translate(translate_table)
                               for k, v in value_map.items()}
        else:
            local_value_map = value_map.copy()

        with open(output_filename, "w") as fp_out:
            try:
                output = t_input.substitute(local_value_map)
            except KeyError:
                for mv in t_input.find_missing_variables(local_value_map):
                    logger.error("Variabele [%s] niet gevonden in %s", mv, template)
                    output = ""
                    has_errors = True
            fp_out.write(output)
        config_files.append(output_filename)
        if has_errors:
            raise Exception("Missing variables detected")
    return config_files


def read_keyvault_values(keyvault_name):
    """Lees alle secrets uit de key vault en retourneer als een Dict."""
    values = {}
    credential = DefaultAzureCredential()

    secret_client = SecretClient(
        vault_url=keyvault_name, credential=credential)
    for secret in secret_client.list_properties_of_secrets():
        values[secret.name] = secret_client.get_secret(secret.name).value

    return values


def read_env_values() -> Dict[str, str]:
    """
    Lees alle secrets uit environment variables en retourneer als een Dict.
    Voeg varianten toe met - ipv _ omdat - niet in environment variables namen mag voorkomen
    """
    values = dict(os.environ)
    values.update({key.replace('_', '-'): value for key,
                  value in values.items()})
    return values


def read_values(keyvault: str = "") -> Dict[str, str]:
    if keyvault:
        value_map = read_keyvault_values(keyvault)
        logger.debug("Read key vault secrets: %s", value_map.keys())
        value_map.update((k, v)
                         for k, v in read_env_values().items() if k in value_map)
        logger.debug("Added environment variable secrets: %s",
                     value_map.keys())
    else:
        value_map = read_env_values()
    return value_map


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--keyvault",
        action=EnvDefault,
        envvar="CONF_KEYVAULT",
        help="Naam van de key vault met configuratiewaarden. Dit overschrijft de environment variable CONF_KEYVAULT",
    )
    parser.add_argument(
        "--rootpath",
        action=EnvDefault,
        envvar="CONF_ROOTPATH",
        default='/etl/config',
        help="Root folder waar de templates gezocht worden. Dit overschrijft de environment variable CONF_ROOTPATH",
    )
    parser.add_argument(
        "--loglevel",
        action='store',
        choices=['DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL'],
        default='INFO',
        help="Loglevel",
    )
    args = parser.parse_args()

    logger.setLevel(args.loglevel)
    logger.info("Configuration started with arguments: %s", args)
    value_map = read_values(args.keyvault)

    template_files = get_templates(args.rootpath)
    logger.info("Gevonden templates: %s", template_files)

    config_files = generate_config_files(template_files, value_map)
    logger.info("Generated config files: %s", config_files)
    logger.info("Configuration finished.")
